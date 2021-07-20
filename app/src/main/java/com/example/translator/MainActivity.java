package com.example.translator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpin, toSpin;
    private TextInputEditText textInputEditText;
    private ImageView imageMic;
    private Button translateButton;
    private TextView translateText;

    private String [] fromLanguage ={"from","English","Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali","Hindi","Catalan","Czech","Marathi","Welsh","German","Urdu"};

    private String [] toLanguage ={"To","English","Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali","Hindi","Catalan","Czech","Marathi","Welsh","German","Urdu","Gujarati","French","Spanish","Japanese","Korean","Chinese","Turkish"};

    private static final int mic_permission_code = 1;
    int languageCode, fromLanguageCode, toLanguageCode ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpin = findViewById(R.id.fromSpin);
        toSpin = findViewById(R.id.toSpin);
        textInputEditText = findViewById(R.id.textInputEditText);
        imageMic = findViewById(R.id.imageMic);
        translateButton = findViewById(R.id.translateButton);
        translateText = findViewById(R.id.translateText);

        fromSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguageCode = getLanguage(fromLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpin.setAdapter(fromAdapter);


        toSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguageCode = getLanguage(toLanguage[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this,R.layout.spinner_item,toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpin.setAdapter(toAdapter);


        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateText.setText("");
                if(textInputEditText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter the text to translate", Toast.LENGTH_SHORT).show();
                }
                 else if (fromLanguageCode == 0){
                    Toast.makeText(MainActivity.this, "Please select the source language", Toast.LENGTH_SHORT).show();
                }
                 else if (toLanguageCode == 0){
                    Toast.makeText(MainActivity.this, "Please select the language to translate", Toast.LENGTH_SHORT).show();
                }
                else {
                    TranslatedText(fromLanguageCode,toLanguageCode,textInputEditText.getText().toString());
                }
            }
        });

        imageMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to translate");

                try {
                    startActivityForResult(intent,mic_permission_code);

                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mic_permission_code){
            if (resultCode == RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                textInputEditText.setText(result.get(0));
            }
        }
    }
    private void TranslatedText(int fromLanguageCode, int toLanguageCode, String source ){
        translateText.setText("Downloading File...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder().setSourceLanguage(fromLanguageCode).setTargetLanguage(toLanguageCode).build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translateText.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translateText.setText(s);
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(MainActivity.this, "Fail to translate", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(MainActivity.this, "Fail to download file", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public int getLanguage(String language){
        int languageCode = 0;

        switch (language){
            case "English" : languageCode  = FirebaseTranslateLanguage.EN;break;
            case "Afrikaans" : languageCode = FirebaseTranslateLanguage.AF;break;
            case "Arabic" : languageCode = FirebaseTranslateLanguage.AR;break;
            case "Belarusian" : languageCode = FirebaseTranslateLanguage.BE; break;
            case "Bulgarian" : languageCode = FirebaseTranslateLanguage.BG; break;
            case "Bengali" : languageCode = FirebaseTranslateLanguage.BN; break;
            case "Hindi" : languageCode = FirebaseTranslateLanguage.HI; break;
            case "Catalan" : languageCode = FirebaseTranslateLanguage.CA; break;
            case "Czech" : languageCode = FirebaseTranslateLanguage.CS; break;
            case "Marathi" : languageCode = FirebaseTranslateLanguage.MR; break;
            case "Welsh" : languageCode = FirebaseTranslateLanguage.CY; break;
            case "German" : languageCode = FirebaseTranslateLanguage.DE; break;
            case "Urdu" : languageCode = FirebaseTranslateLanguage.UR; break;
            case "Gujarati" : languageCode = FirebaseTranslateLanguage.GU; break;
            case "French" : languageCode = FirebaseTranslateLanguage.FR; break;
            case "Spanish" : languageCode = FirebaseTranslateLanguage.ES; break;
            case "Japanese" : languageCode = FirebaseTranslateLanguage.JA; break;
            case "Korean" : languageCode = FirebaseTranslateLanguage.KO; break;
            case "Chinese" : languageCode = FirebaseTranslateLanguage.ZH; break;
            case "Turkish" : languageCode = FirebaseTranslateLanguage.TR; break;
            default: languageCode = 0 ;
        }
        return  languageCode;
    }
}