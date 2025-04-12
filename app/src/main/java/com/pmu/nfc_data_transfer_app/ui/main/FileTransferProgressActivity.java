package com.pmu.nfc_data_transfer_app.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private ProgressBar progressBar;
    private TextView tvProgress;
    private RecyclerView rvFiles;
    private FileProgressAdapter adapter;
    private List<File> files;
    private int totalFiles;
    private int currentFileIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_transfer_progress);

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        rvFiles = findViewById(R.id.rvFiles);

        // Get files from intent
        ArrayList<String> filePaths = getIntent().getStringArrayListExtra("file_paths");
        if (filePaths == null) {
            finish();
            return;
        }

        files = new ArrayList<>();
        for (String path : filePaths) {
            files.add(new File(path));
        }
        totalFiles = files.size();

        setupRecyclerView();
        startFileTransfer();
    }

    private void setupRecyclerView() {
        adapter = new FileProgressAdapter(files);
        rvFiles.setLayoutManager(new LinearLayoutManager(this));
        rvFiles.setAdapter(adapter);
    }

    private void startFileTransfer() {
        // Simulate file transfer progress
        // In a real implementation, this would be your actual file transfer logic
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < files.size(); i++) {
                    final int index = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentFileIndex = index;
                            updateProgress();
                            adapter.notifyItemChanged(index);
                        }
                    });
                    try {
                        Thread.sleep(1000); // Simulate transfer time
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Transfer complete
                        progressBar.setProgress(100);
                        tvProgress.setText("100%");
                    }
                });
            }
        }).start();
    }

    private void updateProgress() {
        int progress = ((currentFileIndex + 1) * 100) / totalFiles;
        progressBar.setProgress(progress);
        tvProgress.setText(progress + "%");
    }

    private static class FileProgressAdapter extends RecyclerView.Adapter<FileProgressAdapter.FileViewHolder> {
        private final List<File> files;
        private final Set<Integer> transferredFiles;

        public FileProgressAdapter(List<File> files) {
            this.files = files;
            this.transferredFiles = new HashSet<>();
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
                holder.ivStatus.setImageResource(R.drawable.ic_check);
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