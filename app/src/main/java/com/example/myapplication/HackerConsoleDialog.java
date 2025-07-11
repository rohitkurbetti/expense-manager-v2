package com.example.myapplication;

// HackerConsoleDialog.java

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class HackerConsoleDialog extends Dialog {

    private TextView textViewConsole;
    private ImageView imageViewHacker;
    private String[] sentences; // Array to hold individual sentences
    private int currentSentenceIndex = 0;
    private int charIndex = 0;
    private long typingDelayMillis = 30; // Delay between each character
    private long sentenceDelayMillis = 1000; // Delay after each sentence (1 second)
    private Handler handler = new Handler();

    public HackerConsoleDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_hacker_console); // Set your custom layout

        textViewConsole = findViewById(R.id.textViewConsole);
//        imageViewHacker = findViewById(R.id.imageViewHacker);

        // Make the dialog non-cancelable by touch outside
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    /**
     * Call this method to start the typing animation for multiple sentences.
     *
     * @param textToType An array of strings, where each string is a sentence.
     */
    public void startTyping(String[] textToType) {
        this.sentences = textToType;
        currentSentenceIndex = 0;
        textViewConsole.setText(""); // Clear previous text
        if (sentences != null && sentences.length > 0) {
            handler.postDelayed(typingRunnable, typingDelayMillis);
        } else {
            // No sentences to type, dismiss or handle accordingly
            dismiss();
        }
    }

    private Runnable typingRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentSentenceIndex < sentences.length) {
                String currentSentence = sentences[currentSentenceIndex];

                if (charIndex < currentSentence.length()) {
                    // Still typing the current sentence
                    textViewConsole.append(String.valueOf(currentSentence.charAt(charIndex)));
                    charIndex++;
                    handler.postDelayed(this, typingDelayMillis); // Schedule next character
                } else {
                    // Current sentence is fully typed
                    textViewConsole.append("\n"); // Move to the next line for the next sentence
                    currentSentenceIndex++; // Move to the next sentence
                    charIndex = 0; // Reset character index for the new sentence

                    if (currentSentenceIndex < sentences.length) {
                        // There are more sentences, apply a delay before starting the next one
                        handler.postDelayed(this, sentenceDelayMillis);
                    } else {
                        // All sentences typed, dismiss the dialog after a final delay
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismiss();
                            }
                        }, 1500); // Dismiss 1.5 seconds after the last sentence
                    }
                }
            }
        }
    };

    // Important: Stop the handler when the dialog is dismissed to prevent memory leaks
    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacks(typingRunnable);
    }
}