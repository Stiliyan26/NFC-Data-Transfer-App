package com.pmu.nfc_data_transfer_app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pmu.nfc_data_transfer_app.R;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileTransferProgressActivity extends AppCompatActivity {
    private static final String TAG = "FileTransferProgress";

    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView statusText;
    private TextView transferSpeedText;
    private TextView currentFileText;
    private ImageView statusIcon;
    private Button doneButton;
    private RecyclerView rvFiles;
    private FileProgressAdapter adapter;
    private List<File> files;
    private int totalFiles;
    private int currentFileIndex;
    private Handler handler;
    private boolean isTransferring = false;
    private long startTime;
    private long totalBytesTransferred = 0;
    private long totalBytesToTransfer = 0;
    private boolean isTransferComplete = false;

    // Error state views
    private View errorState;
    private TextView errorTitle;
    private TextView errorMessage;
    private Button errorDoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transfer_progress);
        Log.d(TAG, "Activity created");

        initializeViews();
        setupHandlers();
        loadFilesFromIntent();
        setupRecyclerView();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else if (!files.isEmpty()) {
            startFileTransfer();
        }
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        statusText = findViewById(R.id.statusText);
        transferSpeedText = findViewById(R.id.transferSpeedText);
        currentFileText = findViewById(R.id.currentFileText);
        statusIcon = findViewById(R.id.statusIcon);
        doneButton = findViewById(R.id.doneButton);
        rvFiles = findViewById(R.id.rvFiles);

        errorState = findViewById(R.id.errorState);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        errorDoneButton = findViewById(R.id.errorDoneButton);
    }

    private void setupHandlers() {
        handler = new Handler(Looper.getMainLooper());

        doneButton.setOnClickListener(v -> {
            if (isTransferComplete) {
                finish();
            }
        });

        errorDoneButton.setOnClickListener(v -> finish());
    }

    private void loadFilesFromIntent() {
        ArrayList<String> filePaths = getIntent().getStringArrayListExtra("file_paths");
        Log.d(TAG, "Received files: " + (filePaths != null ? filePaths.size() : 0));

        files = new ArrayList<>();
        totalBytesToTransfer = 0;

        if (filePaths != null) {
            for (String path : filePaths) {
                File file = new File(path);
                if (file.exists() && file.length() > 0) {
                    files.add(file);
                    totalBytesToTransfer += file.length();
                    Log.d(TAG, "Added file: " + file.getName() + " (" + file.length() + " bytes)");
                }
            }
        }

        totalFiles = files.size();

        if (files.isEmpty()) {
            showError(getString(R.string.no_valid_files));
        }
    }

    private void setupRecyclerView() {
        adapter = new FileProgressAdapter(files);
        rvFiles.setLayoutManager(new LinearLayoutManager(this));
        rvFiles.setAdapter(adapter);
    }

    private void startFileTransfer() {
        if (files.isEmpty()) {
            showError(getString(R.string.no_files_to_transfer));
            return;
        }

        Log.d(TAG, "Starting transfer of " + files.size() + " files");

        isTransferring = true;
        isTransferComplete = false;
        startTime = System.currentTimeMillis();
        updateStatus(getString(R.string.transferring_data), null);
        startFileTransferProcess();
    }

    private void startFileTransferProcess() {
        if (currentFileIndex >= files.size()) {
            showSuccess();
            return;
        }

        File currentFile = files.get(currentFileIndex);
        currentFileText.setText(getString(R.string.transferring_file, currentFile.getName()));

        simulateFileTransfer(currentFile);
    }

    private void simulateFileTransfer(File file) {
        final long fileSize = file.length();
        final int totalSteps = 100;
        final long bytesPerStep = fileSize / totalSteps;

        new Thread(() -> {
            for (int i = 0; i <= totalSteps; i++) {
                if (!isTransferring) {
                    showCancelled();
                    return;
                }

                final int progress = i;
                final long bytesTransferred = i * bytesPerStep;
                totalBytesTransferred = bytesTransferred;

                handler.post(() -> {
                    int overallProgress = (int)((totalBytesTransferred * 100) / totalBytesToTransfer);
                    overallProgress = Math.min(overallProgress, 100);
                    progressBar.setProgress(overallProgress);
                    tvProgress.setText(overallProgress + "%");
                    updateTransferSpeed();
                    adapter.notifyItemChanged(currentFileIndex);
                });

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Transfer interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }

            totalBytesTransferred += fileSize;

            handler.post(() -> {
                adapter.markFileAsTransferred(currentFileIndex);
                currentFileIndex++;
                startFileTransferProcess();
            });
        }).start();
    }

    private void updateTransferSpeed() {
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - startTime;
        if (timeElapsed > 0) {
            double speedMBps = (totalBytesTransferred * 1000.0) / (timeElapsed * 1024 * 1024);
            transferSpeedText.setText(getString(R.string.transfer_speed, speedMBps));
        }
    }

    private void showSuccess() {
        Log.d(TAG, "Transfer completed successfully");
        isTransferring = false;
        isTransferComplete = true;
        handler.post(() -> {
            statusIcon.setImageResource(R.drawable.ic_success);
            statusText.setText(getString(R.string.transfer_complete));
            progressBar.setProgress(100);
            tvProgress.setText("100%");
            doneButton.setVisibility(View.VISIBLE);

            doneButton.setEnabled(false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                doneButton.setEnabled(true);
            }, 1000);
        });
    }

    private void showCancelled() {
        Log.d(TAG, "Transfer cancelled");
        isTransferring = false;
        isTransferComplete = true;
        handler.post(() -> {
            statusIcon.setImageResource(R.drawable.ic_cancelled);
            statusText.setText(getString(R.string.transfer_cancelled));
            doneButton.setVisibility(View.VISIBLE);
        });
    }

    private void showError(String message) {
        Log.e(TAG, "Error: " + message);
        isTransferring = false;
        isTransferComplete = true;

        handler.post(() -> {
            // Hide transfer views
            statusIcon.setVisibility(View.GONE);
            statusText.setVisibility(View.GONE);
            tvProgress.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            currentFileText.setVisibility(View.GONE);
            transferSpeedText.setVisibility(View.GONE);
            rvFiles.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);

            // Show error state
            errorState.setVisibility(View.VISIBLE);
            errorMessage.setText(message);
        });
    }

    private void updateStatus(String status, String transferStatus) {
        handler.post(() -> {
            statusText.setText(status);
            if (transferStatus != null) {
                tvProgress.setText(transferStatus);
            }
        });
    }

    private void restoreState(Bundle savedInstanceState) {
        isTransferring = savedInstanceState.getBoolean("isTransferring", false);
        isTransferComplete = savedInstanceState.getBoolean("isTransferComplete", false);
        currentFileIndex = savedInstanceState.getInt("currentFileIndex", 0);
        totalBytesTransferred = savedInstanceState.getLong("totalBytesTransferred", 0);

        if (isTransferComplete) {
            showSuccess();
        } else if (isTransferring) {
            startFileTransfer();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isTransferring", isTransferring);
        outState.putBoolean("isTransferComplete", isTransferComplete);
        outState.putInt("currentFileIndex", currentFileIndex);
        outState.putLong("totalBytesTransferred", totalBytesTransferred);
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            isTransferring = false;
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (isTransferComplete) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadFilesFromIntent();
    }

    private static class FileProgressAdapter extends RecyclerView.Adapter<FileProgressAdapter.FileViewHolder> {
        private final List<File> files;
        private final Set<Integer> transferredFiles;

        public FileProgressAdapter(List<File> files) {
            this.files = files;
            this.transferredFiles = new HashSet<>();
        }

        public void markFileAsTransferred(int position) {
            transferredFiles.add(position);
            notifyItemChanged(position);
        }

        @NonNull
        @Override
        public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_file_progress, parent, false);
            return new FileViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
            File file = files.get(position);
            holder.tvFileName.setText(file.getName());
            holder.tvFileSize.setText(formatFileSize(file.length()));

            if (transferredFiles.contains(position)) {
                holder.ivStatus.setImageResource(R.drawable.ic_success);
                holder.ivStatus.setColorFilter(holder.itemView.getContext().getColor(R.color.green));
            } else {
                holder.ivStatus.setImageResource(R.drawable.ic_pending);
                holder.ivStatus.setColorFilter(holder.itemView.getContext().getColor(R.color.gray));
            }
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        private String formatFileSize(long size) {
            double kb = size / 1024.0;
            double mb = kb / 1024.0;
            if (mb >= 1.0) {
                return String.format("%.1f MB", mb);
            } else if (kb >= 1.0) {
                return String.format("%.1f KB", kb);
            } else {
                return size + " bytes";
            }
        }

        static class FileViewHolder extends RecyclerView.ViewHolder {
            final ImageView ivFileIcon;
            final TextView tvFileName;
            final TextView tvFileSize;
            final ImageView ivStatus;

            FileViewHolder(View view) {
                super(view);
                ivFileIcon = view.findViewById(R.id.ivFileIcon);
                tvFileName = view.findViewById(R.id.tvFileName);
                tvFileSize = view.findViewById(R.id.tvFileSize);
                ivStatus = view.findViewById(R.id.ivStatus);
            }
        }
    }
}