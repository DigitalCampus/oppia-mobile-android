package org.digitalcampus.oppia.activity;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.commons.io.IOUtils;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityOfflineCourseImportBinding;
import org.digitalcampus.oppia.adapter.OfflineCourseImportAdapter;
import org.digitalcampus.oppia.exception.CourseInstallException;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.OnRemoveButtonClickListener;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.OfflineCourseFile;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.task.result.EntityListResult;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class OfflineCourseImportActivity extends AppActivity implements InstallCourseListener, ScanMediaListener, OnRemoveButtonClickListener {

    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;
    private ActivityOfflineCourseImportBinding binding;
    private OfflineCourseImportAdapter adapter;
    private boolean coursesImported = false;
    private boolean mediaImported = false;

    @Inject
    CoursesRepository coursesRepository;

    private final ArrayList<OfflineCourseFile> files = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOfflineCourseImportBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getAppComponent().inject(this);

        binding.selectFilesBtn.setOnClickListener(view -> launchFileExplorer());
        binding.importCoursesBtn.setOnClickListener(view -> importFiles());

        adapter = new OfflineCourseImportAdapter(this, files, this);
        binding.selectedCourses.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_info) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.offline_course_import_instructions))
                    .setNegativeButton(R.string.back, null)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchFileExplorer() {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("application/zip");
            someActivityResultLauncher.launch(intent);
        }
    }

    private final ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    assert result.getData() != null;
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        List<Uri> uris = getURIsFromResult(result.getData());
                        processSelectedFiles(uris);
                    });
                }
            });

    public List<Uri> getURIsFromResult(Intent data) {
        List<Uri> selectedUris = new ArrayList<>();

        Uri singleUri = data.getData();
        if (singleUri != null) {
            selectedUris.add(singleUri);
        }

        ClipData clipData = data.getClipData();
        if (clipData != null) {
            int count = clipData.getItemCount();
            for (int i = 0; i < count; i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                selectedUris.add(uri);
            }
        }
        return selectedUris;
    }

    private void processSelectedFiles(List<Uri> uris) {
        beginProcessingFiles();
        for (Uri uri : uris) {
            if (uri == null) continue;
            processFile(uri);
        }
        endProcessingFiles();
    }

    private void beginProcessingFiles() {
        runOnUiThread(() -> {
            binding.circleProgress.setVisibility(View.VISIBLE);
            binding.emptyFiles.setVisibility(View.GONE);
        });
    }

    private void processFile(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        File destinationFile = new File(getCacheDir(), fileName);

        if(!destinationFile.exists()) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(uri);
                outputStream = new FileOutputStream(destinationFile);
                IOUtils.copy(inputStream, outputStream);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        if(files.stream().noneMatch(i -> i.getFile().getName().equals(destinationFile.getName()))) {
            files.add(new OfflineCourseFile(destinationFile));
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        }
    }

    private void endProcessingFiles() {
        runOnUiThread(() -> {
            binding.circleProgress.setVisibility(View.GONE);
            if(!files.isEmpty()) {
                binding.importCoursesBtn.setEnabled(true);
                if(files.stream().noneMatch(i -> i.getType() == OfflineCourseFile.FileType.MEDIA)) {
                    mediaImported = true;
                }

                if(files.stream().noneMatch(i -> i.getType() == OfflineCourseFile.FileType.COURSE)) {
                    coursesImported = true;
                }
            } else {
                binding.emptyFiles.setVisibility(View.VISIBLE);
            }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                fileName = cursor.getString(displayNameIndex);
            }
        }
        return fileName;
    }

    private void importFiles() {
        binding.actionButtons.setVisibility(View.GONE);
        binding.title.setVisibility(View.VISIBLE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler();
        executor.execute(() -> {
            moveFilesForImportDependingOnFileType();

            handler.post(() -> {
                importCourses();
                importMedia();
            });
        });
    }

    private void moveFilesForImportDependingOnFileType() {
        for (int i = 0; i < files.size(); i++) {
            OfflineCourseFile item = files.get(i);
            item.updateStatus(OfflineCourseFile.Status.IMPORTING);
            int itemPosition = i;
            runOnUiThread(() -> {
                adapter.notifyItemChanged(itemPosition);
                updateImportProgressText("Preparing file for import: " + item.getFile().getName());
            });

            switch(item.getType()){
                case COURSE: {
                    FileUtils.copyFile(item.getFile(), new File(Storage.getDownloadPath(this)));
                    break;
                }
                case MEDIA: {
                    try {
                        FileUtils.unzipFiles(this, item.getFile().getParent(), item.getFile().getName(), Storage.getMediaPath(this));
                    } catch (CourseInstallException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    private void importCourses() {
        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(this);
        imTask.setInstallerListener(this);
        imTask.execute();
    }

    private void importMedia() {
        ScanMediaTask smTask = new ScanMediaTask(this);
        smTask.setScanMediaListener(this);
        smTask.execute(coursesRepository.getCourses(this));
    }

    @Override
    public void installComplete(BasicResult result) {
        updateImportProgressText(result.getResultMessage());
        for (OfflineCourseFile item : files) {
            if(item.getType() == OfflineCourseFile.FileType.COURSE) {
                item.updateStatus(OfflineCourseFile.Status.IMPORTED);
            }
        }
        adapter.notifyDataSetChanged();


        coursesImported = true;
        if(mediaImported) {
            binding.importInfo.setVisibility(View.GONE);
            binding.title.setText(getString(R.string.offline_course_import_import_completed));
        } else {
            updateImportProgressText(getString(R.string.offline_course_import_course_files_imported));
        }
    }

    @Override
    public void installProgressUpdate(DownloadProgress dp) {
        updateImportProgressText(dp.getMessage());
    }

    @Override
    public void onRemoveButtonClick(int position) {
        if (position >= 0 && position < files.size()) {
            files.remove(position);
            adapter.notifyItemRemoved(position);

            if (files.isEmpty()) {
                binding.emptyFiles.setVisibility(View.VISIBLE);
                binding.importCoursesBtn.setEnabled(false);
            }
        }
    }

    private void updateImportProgressText(String message) {
        if(binding.importInfo.getVisibility() == View.GONE) {
            binding.importInfo.setVisibility(View.VISIBLE);
        }
        binding.importInfo.setText(message);
    }

    @Override
    public void scanStart() {}

    @Override
    public void scanProgressUpdate(String msg) {
        updateImportProgressText(getString(R.string.offline_course_import_importing_media_file, msg));
    }

    @Override
    public void scanComplete(EntityListResult<Media> result) {
        for (OfflineCourseFile item : files) {
            if(item.getType() == OfflineCourseFile.FileType.MEDIA) {
                item.updateStatus(OfflineCourseFile.Status.IMPORTED);
            }
        }
        adapter.notifyDataSetChanged();

        mediaImported = true;
        if(coursesImported) {
            binding.importInfo.setVisibility(View.GONE);
            binding.title.setText(R.string.offline_course_import_import_completed);
        } else {
            updateImportProgressText(getString(R.string.offline_course_import_media_files_imported));
        }

    }
}
