package com.mahigeeks.mynotesapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mahigeeks.mynotesapp.Models.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesTakerActivity extends AppCompatActivity{
    EditText editText_title, editText_notes;
    ImageView imageView_save,imageButton_share,imageButton_copy;
    Notes notes;
    boolean isOldNote = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_taker);

        imageView_save = findViewById(R.id.imageView_save);
        editText_title = findViewById(R.id.editText_title);
        editText_notes = findViewById(R.id.editText_notes);
        imageButton_share = findViewById(R.id.imageButton_share);
        imageButton_copy = findViewById(R.id.imageButton_copy);

        imageButton_share.setOnClickListener(view -> {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, editText_title.getText().toString() + editText_notes.getText().toString());
            shareIntent.setType("text/plain");
            shareIntent = Intent.createChooser(shareIntent,"Share via:");
            startActivity(shareIntent);
        });

        imageButton_copy.setOnClickListener(view -> {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData data = ClipData.newPlainText("text",editText_title.getText().toString() + editText_notes.getText().toString());
            clipboardManager.setPrimaryClip(data);

            Toast.makeText(NotesTakerActivity.this,"Text copied",Toast.LENGTH_SHORT).show();
        });

        notes = new Notes();
        try {
            notes = (Notes) getIntent().getSerializableExtra("old_note");
            editText_title.setText(notes.getTitle());
            editText_notes.setText(notes.getNotes());
            isOldNote = true;
        } catch(Exception e) {
            e.printStackTrace();
        }


        imageView_save.setOnClickListener(this::onClick);
    }

    private void onClick(View v) {
        String title = editText_title.getText().toString();
        String description = editText_notes.getText().toString();

        if (description.isEmpty()) {
            Toast.makeText(NotesTakerActivity.this, "Please add some notes", Toast.LENGTH_SHORT).show();
            return;
        }
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss a");
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
}