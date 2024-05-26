package com.mahigeeks.mynotesapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mahigeeks.mynotesapp.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NotesTakerActivity extends AppCompatActivity {
    EditText editText_title, editText_notes;
    ImageView imageView_save;
    ImageButton micButtonTitle, micButtonNotes, imageButton_share, imageButton_copy;
    Notes notes;
    boolean isOldNote = false;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);

        imageView_save = findViewById(R.id.imageView_save);
        editText_title = findViewById(R.id.editText_title);
        editText_notes = findViewById(R.id.editText_notes);
        imageButton_copy = findViewById(R.id.imageButton_copy);
        imageButton_share = findViewById(R.id.imageButton_share);
        micButtonTitle = findViewById(R.id.micButtonTitle);
        micButtonNotes = findViewById(R.id.micButtonNotes);

        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("old_name");
            editText_title.setText(notes.getTitle());
            editText_notes.setText(notes.getNotes());
            isOldNote = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize the SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Request audio recording permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        micButtonTitle.setOnClickListener(v -> {
            startSpeechToText(1); // 1 for title
        });

        micButtonNotes.setOnClickListener(v -> {
            startSpeechToText(2); // 2 for notes
        });

        imageButton_copy.setOnClickListener(v -> copyTextToClipboard());

        imageButton_share.setOnClickListener(v -> shareNote());

        imageView_save.setOnClickListener(v -> saveNote());
    }

    private void startSpeechToText(int inputType) {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                Toast.makeText(NotesTakerActivity.this, "Error recognizing speech", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String text = matches.get(0);
                    if (inputType == 1) {
                        editText_title.setText(text);
                    } else if (inputType == 2) {
                        editText_notes.setText(text);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        speechRecognizer.startListening(speechRecognizerIntent);
    }

    private void copyTextToClipboard() {
        // Get the text from the EditText
        String textToCopy = editText_notes.getText().toString();

        // Get the Clipboard Manager
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // Create a ClipData with the text to copy
        ClipData clipData = ClipData.newPlainText("text", textToCopy);

        // Set the ClipData to the Clipboard
        clipboardManager.setPrimaryClip(clipData);

        // Show a Toast message to indicate the text has been copied
        Toast.makeText(NotesTakerActivity.this, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void shareNote() {
        String title = editText_title.getText().toString();
        String description = editText_notes.getText().toString();

        if (description.isEmpty()) {
            Toast.makeText(NotesTakerActivity.this, "Please add some notes", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareContent = "Title: " + title + "\n\n" + "Notes: " + description;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        startActivity(Intent.createChooser(shareIntent, "Share notes via"));
    }

    private void saveNote() {
        String title = editText_title.getText().toString();
        String description = editText_notes.getText().toString();

        if (description.isEmpty()) {
            Toast.makeText(NotesTakerActivity.this, "Please add some notes", Toast.LENGTH_SHORT).show();
            return;
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss a");
        Date date = new Date();

        if (!isOldNote) {
            notes = new Notes();
        }

        notes.setTitle(title);
        notes.setNotes(description);
        notes.setDate(formatter.format(date));

        Intent intent = new Intent();
        intent.putExtra("note", notes);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission to record audio is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
