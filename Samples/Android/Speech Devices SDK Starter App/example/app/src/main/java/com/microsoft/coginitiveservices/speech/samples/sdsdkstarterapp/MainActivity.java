package com.microsoft.coginitiveservices.speech.samples.sdsdkstarterapp;


import android.content.Intent;

import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.cognitiveservices.speech.KeywordRecognitionModel;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.intent.IntentRecognitionResult;
import com.microsoft.cognitiveservices.speech.intent.IntentRecognizer;
import com.microsoft.cognitiveservices.speech.intent.LanguageUnderstandingModel;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static com.microsoft.cognitiveservices.speech.ResultReason.RecognizedKeyword;
import static com.microsoft.cognitiveservices.speech.ResultReason.RecognizingSpeech;
import static junit.framework.Assert.assertNotNull;

public class MainActivity extends AppCompatActivity {

    // Subscription
    private static final String SpeechSubscriptionKey = "918301eb1a8e40dcb9b6968fa4a2d027";
    private static final String SpeechRegion = "westus"; // You can change this, if you want to test the intent, and your LUIS region is different.
    private static final String LuisSubscriptionKey = "";
    private static final String LuisRegion = "westus2"; // you can change this, if you want to test the intent, and your LUIS region is different.
    private static final String LuisAppId = "43929081ad0a47fba3294533cf1af342";

    private static final String Keyword = "Computer";
    private static final String KeywordModel = "kws-computer.zip";
    private static String DeviceGeometry = "Linear4";
     private static String SelectedGeometry = "Linear4";
    //private static final String DeviceGeometry = "Circular6+1";
   // private static final String SelectedGeometry = "Circular6+1";


    // Note: point this to a wav file in case you don't want to
    //       use the microphone. It will be used automatically, if
    //       the file exists on disk.
    private static final String SampleAudioInput = "/data/keyword/kws-computer.wav";

    private TextView recognizedTextView;
    private Button recognizeIntermediateButton;
    private Button recognizeContinuousButton;
    private Button recognizeKwsButton;
    private Button recognizeIntentButton;
    private Button recognizeIntentKwsButton;
    private Button selectRecoLanguageButton;
    private Button selectTranLanguageButton;
    private Button meetingButton;
    private Button ttsButton;
    private Button translateButton;
    private TextView recognizeLanguageTextView;
    private TextView translateLanguageTextView;

    private Toolbar mainToolbar;
    private final HashMap<String, String> intentIdMap = new HashMap<>();
    private static String languageRecognition = "en-US";
	private static String translateLanguage = "zh-Hans";
	private HashMap<String, String> mapRecolanguageCode = new HashMap<>();
	private HashMap<String, String> mapTranlanguageCode = new HashMap<>();
    static final int SELECT_RECOGNIZE_LANGUAGE_REQUEST = 0;  // The request code
    static final int SELECT_TRANSLATE_LANGUAGE_REQUEST = 1;  // The request code


    private AudioConfig getAudioConfig() {
        if(new File(SampleAudioInput).exists()) {
            recognizedTextView.setText(recognizedTextView.getText() + "\nInfo: Using AudioFile " + SampleAudioInput);

            // run from a file
            return AudioConfig.fromWavFileInput(SampleAudioInput);
        }

        // run from the microphone
        return AudioConfig.fromDefaultMicrophoneInput();
    }

    private SpeechConfig getSpeechConfig() {
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);

        // PMA parameters
        speechConfig.setProperty("DeviceGeometry", DeviceGeometry);
        speechConfig.setProperty("SelectedGeometry", SelectedGeometry);
        speechConfig.setSpeechRecognitionLanguage(languageRecognition);

        return speechConfig;
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.settingmenu,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.RecoLanguage : {
                Intent selectLanguageIntent = new Intent(MainActivity.this,listLanguage.class);
                selectLanguageIntent.putExtra("RecognizeOrTranslate", 0);
                startActivityForResult(selectLanguageIntent, SELECT_RECOGNIZE_LANGUAGE_REQUEST);
                return true;
            }
            case R.id.TranLanguage :{
                Intent selectLanguageIntent = new Intent(MainActivity.this, listLanguage.class);
                selectLanguageIntent.putExtra("RecognizeOrTranslate", 1);
                startActivityForResult(selectLanguageIntent, SELECT_TRANSLATE_LANGUAGE_REQUEST);
                return true;
            }
            case R.id.LinearDevice :{
                DeviceGeometry = "Linear4";
                SelectedGeometry = "Linear4";
                return true;
            }
            case R.id.CircularDevice:{
                DeviceGeometry = "Circular6+1";
                SelectedGeometry = "Circular6+1";
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recognizedTextView = findViewById(R.id.recognizedText);
        recognizeIntermediateButton = findViewById(R.id.buttonRecognizeIntermediate);
        recognizeContinuousButton = findViewById(R.id.buttonRecognizeContinuous);
        recognizeKwsButton = findViewById(R.id.buttonRecognizeKws);
        recognizeIntentButton = findViewById(R.id.buttonRecognizeIntent);
        recognizeIntentKwsButton = findViewById(R.id.buttonRecognizeIntentKws);
        recognizedTextView.setMovementMethod(new ScrollingMovementMethod());
        translateButton = findViewById(R.id.buttonTranslate);
        selectRecoLanguageButton = findViewById(R.id.buttonSelectRecoLanguage);
        selectTranLanguageButton = findViewById(R.id.buttonSelectTranLanguage);
        recognizeLanguageTextView = findViewById(R.id.textViewRecognitionLanguage);
        translateLanguageTextView = findViewById(R.id.textViewTranslateLanguage);
        ttsButton = findViewById(R.id.buttonTts);
        meetingButton = findViewById(R.id.buttonMeeting);
        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);



        ///////////////////////////////////////////////////
        // check if we have a valid key
        ///////////////////////////////////////////////////
        if (SpeechSubscriptionKey.startsWith("<") || SpeechSubscriptionKey.endsWith(">")) {
            recognizedTextView.setText("Error: Replace SpeechSubscriptionKey with your actual subscription key and re-compile this application!");
            return;
        }

        if (LuisSubscriptionKey.startsWith("<") || LuisSubscriptionKey.endsWith(">")) {
            recognizedTextView.setText(recognizedTextView.getText() + "\nWarning: Replace LuisSubscriptionKey with your actual Luis subscription key to use Intents!");
        }

        // save the asset manager
        final AssetManager assets = this.getAssets();
        //set recognize language code
        {
            mapRecolanguageCode.put("English (United States)", "en-US");
            mapRecolanguageCode.put("German (Germany)", "de-DE");
            mapRecolanguageCode.put("Chinese (Mandarin, simplified)", "zh-CN");
            mapRecolanguageCode.put("English (India)", "en-IN");
            mapRecolanguageCode.put("Spanish (Spain)", "es-ES");
            mapRecolanguageCode.put("French (France)", "fr-FR");
            mapRecolanguageCode.put("Italian (Italy)", "it-IT");
            mapRecolanguageCode.put("Portuguese (Brazil)", "pt-BR");
            mapRecolanguageCode.put("Russian (Russia)", "ru-RU");
        }

        //set translate language code
        {
            mapTranlanguageCode.put("Afrikaans", "af");
            mapTranlanguageCode.put("Arabic", "ar");
            mapTranlanguageCode.put("Bangla", "bn");
            mapTranlanguageCode.put("Bosnian (Latin)", "bs");
            mapTranlanguageCode.put("Bulgarian", "bg");
            mapTranlanguageCode.put("Cantonese (Traditional)", "yue");
            mapTranlanguageCode.put("Catalan", "ca");
            mapTranlanguageCode.put("Chinese Simplified", "zh-Hans");
            mapTranlanguageCode.put("Chinese Traditional", "zh-Hant");
            mapTranlanguageCode.put("Croatian", "hr");
            mapTranlanguageCode.put("Czech", "cs");
            mapTranlanguageCode.put("Danish", "da");
            mapTranlanguageCode.put("Dutch", "nl");
            mapTranlanguageCode.put("English", "en");
            mapTranlanguageCode.put("Estonian", "et");
            mapTranlanguageCode.put("Fijian", "fj");
            mapTranlanguageCode.put("Filipino", "fil");
            mapTranlanguageCode.put("Finnish", "fi");
            mapTranlanguageCode.put("French", "fr");
            mapTranlanguageCode.put("German", "de");
            mapTranlanguageCode.put("Greek", "el");
            mapTranlanguageCode.put("Haitian Creole", "ht");
            mapTranlanguageCode.put("Hebrew", "he");
            mapTranlanguageCode.put("Hindi", "hi");
            mapTranlanguageCode.put("Hmong Daw", "mww");
            mapTranlanguageCode.put("Hungarian", "hu");
            mapTranlanguageCode.put("Indonesian", "id");
            mapTranlanguageCode.put("Italian", "it");
            mapTranlanguageCode.put("Japanese", "ja");
            mapTranlanguageCode.put("Kiswahili", "sw");
            mapTranlanguageCode.put("Klingon", "tlh");
            mapTranlanguageCode.put("Klingon (plqaD)", "tlh-Qaak");
            mapTranlanguageCode.put("Korean", "ko");
            mapTranlanguageCode.put("Latvian", "lv");
            mapTranlanguageCode.put("Lithuanian", "lt");
            mapTranlanguageCode.put("Malagasy", "mg");
            mapTranlanguageCode.put("Malay", "ms");
            mapTranlanguageCode.put("Maltese", "mt");
            mapTranlanguageCode.put("Norwegian", "nb");
            mapTranlanguageCode.put("Persian", "fa");
            mapTranlanguageCode.put("Polish", "pl");
            mapTranlanguageCode.put("Portuguese", "pt");
            mapTranlanguageCode.put("Queretaro Otomi", "otq");
            mapTranlanguageCode.put("Romanian", "ro");
            mapTranlanguageCode.put("Russian", "ru");
            mapTranlanguageCode.put("Samoan", "sm");
            mapTranlanguageCode.put("Serbian (Cyrillic)", "sr-Cyrl");
            mapTranlanguageCode.put("Serbian (Latin)", "sr-Latn");
            mapTranlanguageCode.put("Slovak", "sk");
            mapTranlanguageCode.put("Slovenian", "sl");
            mapTranlanguageCode.put("Spanish", "es");
            mapTranlanguageCode.put("Swedish", "sv");
            mapTranlanguageCode.put("Tahitian", "ty");
            mapTranlanguageCode.put("Tamil", "ta");
            mapTranlanguageCode.put("Thai", "th");
            mapTranlanguageCode.put("Tongan", "to");
            mapTranlanguageCode.put("Turkish", "tr");
            mapTranlanguageCode.put("Ukrainian", "uk");
            mapTranlanguageCode.put("Urdu", "ur");
            mapTranlanguageCode.put("Vietnamese", "vi");
            mapTranlanguageCode.put("Welsh", "cy");
            mapTranlanguageCode.put("Yucatec Maya", "yua");
        }

        //select recognize language
        selectRecoLanguageButton.setOnClickListener(view -> {

            Intent selectLanguageIntent = new Intent(this, listLanguage.class);
            selectLanguageIntent.putExtra("RecognizeOrTranslate", 0);
            startActivityForResult(selectLanguageIntent, SELECT_RECOGNIZE_LANGUAGE_REQUEST);
        });

        //select translate language
        selectTranLanguageButton.setOnClickListener(view -> {

            Intent selectLanguageIntent = new Intent(this, listLanguage.class);
            selectLanguageIntent.putExtra("RecognizeOrTranslate", 1);
            startActivityForResult(selectLanguageIntent, SELECT_TRANSLATE_LANGUAGE_REQUEST);
        });


        ///////////////////////////////////////////////////
        // recognize Once with intermediate results
        ///////////////////////////////////////////////////
        recognizeIntermediateButton.setOnClickListener(view -> {
            final String logTag = "reco 1";

            disableButtons();
            clearTextBox();
            if(!checkSystemTime()) return;

            try {
                final SpeechRecognizer reco = new SpeechRecognizer(this.getSpeechConfig(), this.getAudioConfig());
                reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                    final String s = speechRecognitionResultEventArgs.getResult().getText();
                    Log.i(logTag, "Intermediate result received: " + s);
                    setRecognizedText(s);
                });

                final Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
                setOnTaskCompletedListener(task, result -> {
                    final String s = result.getText();
                    reco.close();
                    Log.i(logTag, "Recognizer returned: " + s);
                    setRecognizedText(s);
                    enableButtons();
                });
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                displayException(ex);
            }
        });

        ///////////////////////////////////////////////////
        // recognize continuously
        ///////////////////////////////////////////////////
        recognizeContinuousButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "reco 2";
            private boolean continuousListeningStarted = false;
            private SpeechRecognizer reco = null;
            private String buttonText = "";
            private ArrayList<String> content = new ArrayList<>();

            @Override
            public void onClick(final View view) {
                final Button clickedButton = (Button) view;
                disableButtons();
                if(!checkSystemTime()) return;

                if (continuousListeningStarted) {
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            MainActivity.this.runOnUiThread(() -> clickedButton.setText(buttonText));
                            enableButtons();
                            continuousListeningStarted = false;
                        });
                    } else {
                        continuousListeningStarted = false;
                    }

                    return;
                }

                clearTextBox();

                try {
                    content.clear();
                    reco = new SpeechRecognizer(getSpeechConfig(), getAudioConfig());    
                    reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Intermediate result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                        content.remove(content.size() - 1);
                    });

                    reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Final result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                    });

                    final Future<Void> task = reco.startContinuousRecognitionAsync();
                    setOnTaskCompletedListener(task, result -> {
                        continuousListeningStarted = true;
                        MainActivity.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });

        ///////////////////////////////////////////////////
        // recognize with wake word
        ///////////////////////////////////////////////////
        recognizeKwsButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "kws";
            private static final String delimiter = "\n";
            private final ArrayList<String> content = new ArrayList<>();
            private boolean continuousListeningStarted = false;
            private SpeechRecognizer reco = null;
            private String buttonText = "";

            @Override
            public void onClick(View view) {
                final Button clickedButton = (Button) view;
                disableButtons();
                if(!checkSystemTime()) return;

                if (continuousListeningStarted) {
                    if (reco != null) {
                        final Future<Void> task = reco.stopKeywordRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            MainActivity.this.runOnUiThread(() -> clickedButton.setText(buttonText));
                            enableButtons();
                            continuousListeningStarted = false;
                        });
                    } else {
                        continuousListeningStarted = false;
                    }

                    return;
                }

                clearTextBox();

                content.clear();
                content.add("");
                content.add("");
                try {
                    reco = new SpeechRecognizer(getSpeechConfig(), getAudioConfig());
                    reco.sessionStarted.addEventListener((o, sessionEventArgs) -> {
                        Log.i(logTag, "got a session (" + sessionEventArgs.getSessionId() + ")event: sessionStarted");

                        content.set(0, "KeywordModel `" + Keyword + "` detected");
                        setRecognizedText(TextUtils.join(delimiter, content));

                    });

                    reco.sessionStopped.addEventListener((o, sessionEventArgs) -> Log.i(logTag, "got a session (" + sessionEventArgs.getSessionId() + ")event: sessionStopped"));

                    reco.recognizing.addEventListener((o, intermediateResultEventArgs) -> {
                        final String s = intermediateResultEventArgs.getResult().getText();
                        ResultReason rr =intermediateResultEventArgs.getResult().getReason();
                        Log.i(logTag, "got a intermediate result: " + s + " result reason:" + rr.toString());
                        if(rr == RecognizingSpeech) {
                            Integer index = content.size() - 2;
                            content.set(index + 1, index.toString() + ". " + s);
                            setRecognizedText(TextUtils.join(delimiter, content));
                        }
                    });
                    reco.recognized.addEventListener((o, finalResultEventArgs) -> {
                        String s = finalResultEventArgs.getResult().getText();
                        ResultReason rr = finalResultEventArgs.getResult().getReason();

                        if(rr == RecognizedKeyword) {
                            content.add("");
                        }

                        if(  !s.isEmpty() ) {
                            Integer index = content.size() - 2;
                            content.set(index + 1, index.toString() + ". " + s);
                            content.set(0, "say `" + Keyword + "`...");
                            setRecognizedText(TextUtils.join(delimiter, content));
                            Log.i(logTag, "got a final result: " +" " + Integer.toString(index +1) + " " + s + " result reason:" + rr.toString());
                        }

                    });

                    final KeywordRecognitionModel keywordRecognitionModel = KeywordRecognitionModel.fromStream(assets.open(KeywordModel), Keyword, true);

                    final Future<Void> task = reco.startKeywordRecognitionAsync(keywordRecognitionModel);
                    setOnTaskCompletedListener(task, result -> {
                        content.set(0, "say `" + Keyword + "`...");
                        setRecognizedText(TextUtils.join(delimiter, content));
                        continuousListeningStarted = true;
                        MainActivity.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });


        intentIdMap.put("1", "play music");
        intentIdMap.put("2", "stop");

        ///////////////////////////////////////////////////
        // recognize intent
        ///////////////////////////////////////////////////
        recognizeIntentButton.setOnClickListener(view -> {
            final String logTag = "intent";
            final ArrayList<String> content = new ArrayList<>();

            disableButtons();
            clearTextBox();
            if(!checkSystemTime()) return;

            content.add("");
            content.add("");
            try {
                final SpeechConfig speechIntentConfig = SpeechConfig.fromSubscription(LuisSubscriptionKey, LuisRegion);
                speechIntentConfig.setSpeechRecognitionLanguage(languageRecognition);
                IntentRecognizer reco = new IntentRecognizer(speechIntentConfig, this.getAudioConfig());

                LanguageUnderstandingModel intentModel = LanguageUnderstandingModel.fromAppId(LuisAppId);
                for (Map.Entry<String, String> entry : intentIdMap.entrySet()) {
                    reco.addIntent(intentModel, entry.getValue(), entry.getKey());
                    Log.i(logTag, "intent: " + entry.getValue() + " Intent Id : " + entry.getKey());
                }

                reco.recognizing.addEventListener((o, intentRecognitionResultEventArgs) -> {
                    final String s = intentRecognitionResultEventArgs.getResult().getText();
                    Log.i(logTag, "Intermediate result received: " + s);
                    content.set(0, s);
                    setRecognizedText(TextUtils.join("\n", content));
                });

                final Future<IntentRecognitionResult> task = reco.recognizeOnceAsync();
                setOnTaskCompletedListener(task, result -> {
                    Log.i(logTag, "Continuous recognition stopped.");
                    String s = result.getText();
                    String intentId = result.getIntentId();
                    Log.i(logTag, "IntentId: " + intentId);
                    String intent = "";
                    if (intentIdMap.containsKey(intentId)) {
                        intent = intentIdMap.get(intentId);

                    }
                    Log.i(logTag, "S: " + s + ", intent: " + intent);
                    content.set(0, s);
                    content.set(1, " [intent: " + intent + "]");
                    setRecognizedText(TextUtils.join("\n", content));
                    enableButtons();
                });
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                displayException(ex);
            }
        });

        ///////////////////////////////////////////////////
        // recognize intent with wake word
        ///////////////////////////////////////////////////
        recognizeIntentKwsButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "kws intent";
            private static final String delimiter = "\n";
            private final ArrayList<String> content = new ArrayList<>();
            private boolean continuousListeningStarted = false;
            private IntentRecognizer reco = null;
            private String buttonText = "";

            @Override
            public void onClick(View view) {
                final Button clickedButton = (Button) view;
                disableButtons();
                if(!checkSystemTime()) return;

                if (continuousListeningStarted) {
                    if (reco != null) {
                        final Future<Void> task = reco.stopKeywordRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            MainActivity.this.runOnUiThread(() -> clickedButton.setText(buttonText));
                            enableButtons();
                            continuousListeningStarted = false;
                        });
                    } else {
                        continuousListeningStarted = false;
                    }

                    return;
                }

                clearTextBox();

                content.clear();
                content.add("");
                content.add("");
                try {
                    final SpeechConfig intentSpeechConfig = SpeechConfig.fromSubscription(LuisSubscriptionKey, LuisRegion);
                    intentSpeechConfig.setSpeechRecognitionLanguage(languageRecognition);
                    reco = new IntentRecognizer(intentSpeechConfig, getAudioConfig());

                    LanguageUnderstandingModel intentModel = LanguageUnderstandingModel.fromAppId(LuisAppId);
                    for (Map.Entry<String, String> entry : intentIdMap.entrySet()) {
                        reco.addIntent(intentModel, entry.getValue(), entry.getKey());
                        Log.i(logTag, "intent: " + entry.getValue() + " Intent Id : " + entry.getKey());
                    }

                    reco.sessionStarted.addEventListener((o, sessionEventArgs) -> {
                        Log.i(logTag, "got a session (" + sessionEventArgs.getSessionId() + ")event: sessionStarted");
                        content.set(0, "KeywordModel `" + Keyword + "` detected");
                        setRecognizedText(TextUtils.join(delimiter, content));

                    });

                    reco.sessionStopped.addEventListener((o, sessionEventArgs) -> Log.i(logTag, "got a session (" + sessionEventArgs.getSessionId() + ")event: sessionStopped"));

                    reco.recognizing.addEventListener((o, intermediateResultEventArgs) -> {
                        final String s = intermediateResultEventArgs.getResult().getText();
                        ResultReason rr =intermediateResultEventArgs.getResult().getReason();
                        Log.i(logTag, "got a intermediate result: " + s + " result reason:" + rr.toString());
                        if(rr == RecognizingSpeech) {
                            Integer index = content.size() - 2;
                            content.set(index + 1, index.toString() + ". " + s);
                            setRecognizedText(TextUtils.join(delimiter, content));
                        }
                    });

                    reco.recognized.addEventListener((o, finalResultEventArgs) -> {
                        String s = finalResultEventArgs.getResult().getText();
                        String intentId = finalResultEventArgs.getResult().getIntentId();
                        Log.i(logTag, "IntentId: " + intentId);
                        String intent = "";
                        if (intentIdMap.containsKey(intentId)) {
                            intent = intentIdMap.get(intentId);

                        }

                        ResultReason rr = finalResultEventArgs.getResult().getReason();

                        if(rr == RecognizedKeyword) {
                            content.add("");
                        }

                        if( !s.isEmpty() ) {
                            Integer index = content.size() - 2;
                            content.set(index + 1, index.toString() + ". " + s + " [intent: " + intent + "]");
                            content.set(0, "say `" + Keyword + "`...");
                            setRecognizedText(TextUtils.join(delimiter, content));
                            Log.i(logTag, "got a final result: " + " " + Integer.toString(index+1)+" "+ s + " result reason:" + rr.toString());
                        }
                    });

                    final KeywordRecognitionModel keywordRecognitionModel = KeywordRecognitionModel.fromStream(assets.open(KeywordModel), Keyword, true);
                    final Future<Void> task = reco.startKeywordRecognitionAsync(keywordRecognitionModel);
                    setOnTaskCompletedListener(task, result -> {
                        content.set(0, "say `" + Keyword + "`...");
                        setRecognizedText(TextUtils.join(delimiter, content));
                        continuousListeningStarted = true;
                        MainActivity.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });
        ///////////////////////////////////////////////////
        // Neural TTS
        ///////////////////////////////////////////////////
        ttsButton.setOnClickListener( view -> {
            if(!checkSystemTime()) return;
            /*
            try{
                SpeechConfig speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
                speechConfig.setSpeechSynthesisVoiceName("Microsoft Server Speech Text to Speech Voice (en-GB, HazelRUS)");
                SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig);
                assertNotNull(synthesizer);
                SpeechSynthesisResult result1 = synthesizer.SpeakTextAsync(" You can use the Speech SDK to add speech-to-text (speech recognition/SR), intent, translation, and text-to-speech (TTS) capabilities to your apps. The Speech Services also have REST APIs that works with any programming language that can make HTTP requests.").get();
                // "{{{text1}}}" has now completed rendering to default speakers
                 byte[] wav = result1.getAudioData();
                 result1.close();
                synthesizer.close();
                speechConfig.close();
                  //play the audio file
                AudioPlayingThread audioplay = new AudioPlayingThread();
                audioplay.write(wav);
                audioplay.run();
            } catch(Exception ex){
                    System.out.println(ex.getMessage());
                    displayException(ex);
            }
            */
        });


        ///////////////////////////////////////////////////
        // Meeting
        ///////////////////////////////////////////////////
        meetingButton.setOnClickListener(view ->{
            if(!checkSystemTime()) return;
            Intent meetingIntent = new Intent(this, enrollment.class);
            startActivity(meetingIntent);

        });
        ///////////////////////////////////////////////////
        // recognize and translate
        ///////////////////////////////////////////////////
        translateButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "reco t";
            private boolean continuousListeningStarted = false;
            private TranslationRecognizer reco = null;
            private String buttonText = "";
            private ArrayList<String> content = new ArrayList<>();


            @Override
            public void onClick(final View view) {
                final Button clickedButton = (Button) view;
                disableButtons();
                if(!checkSystemTime()) return;

                if (continuousListeningStarted) {
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            MainActivity.this.runOnUiThread(() -> clickedButton.setText(buttonText));
                            enableButtons();
                            continuousListeningStarted = false;
                        });
                    } else {
                        continuousListeningStarted = false;
                    }

                    return;
                }

                clearTextBox();

                try {
                    content.clear();
                    final SpeechTranslationConfig translationSpeechConfig = SpeechTranslationConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
                    translationSpeechConfig.addTargetLanguage(languageRecognition);
                    translationSpeechConfig.addTargetLanguage(translateLanguage);
                    translationSpeechConfig.setSpeechRecognitionLanguage(languageRecognition);
                    reco = new TranslationRecognizer(translationSpeechConfig, getAudioConfig());

                    reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final Map<String, String> translations = speechRecognitionResultEventArgs.getResult().getTranslations();
                        StringBuffer sb = new StringBuffer();
                        for (String key : translations.keySet()) {
                            sb.append( key + " -> '" + translations.get(key) + "'\n");
                        }
                        final String s = sb.toString();

                        Log.i(logTag, "Intermediate result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                        content.remove(content.size() - 1);
                    });

                    reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final Map<String, String> translations = speechRecognitionResultEventArgs.getResult().getTranslations();
                        StringBuffer sb = new StringBuffer();
                        for (String key : translations.keySet()) {
                            sb.append( key + " -> '" + translations.get(key) + "'\n");
                        }
                        final String s = sb.toString();

                        Log.i(logTag, "Final result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                    });

                    final Future<Void> task = reco.startContinuousRecognitionAsync();
                    setOnTaskCompletedListener(task, result -> {
                        continuousListeningStarted = true;
                        MainActivity.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });
    }

    private void displayException(Exception ex) {
        recognizedTextView.setText(ex.getMessage() + "\n" + TextUtils.join("\n", ex.getStackTrace()));
    }

    private void clearTextBox() {
        setTextbox("");
    }

    private void setRecognizedText(final String s) {
        setTextbox(s);
    }

    private void setTextbox(final String s) {
        MainActivity.this.runOnUiThread(() -> {
            recognizedTextView.setText(s);

            final Layout layout = recognizedTextView.getLayout();
            if(layout != null) {
                int scrollDelta = layout.getLineBottom(recognizedTextView.getLineCount() - 1)
                        - recognizedTextView.getScrollY() - recognizedTextView.getHeight();
                if (scrollDelta > 0)
                    recognizedTextView.scrollBy(0, scrollDelta);
            }
        });
    }

    private void disableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            recognizeIntermediateButton.setEnabled(false);
            recognizeContinuousButton.setEnabled(false);
            recognizeKwsButton.setEnabled(false);
            recognizeIntentButton.setEnabled(false);
            recognizeIntentKwsButton.setEnabled(false);
            translateButton.setEnabled(false);
        });
    }

    private void enableButtons() {
        MainActivity.this.runOnUiThread(() -> {
            recognizeIntermediateButton.setEnabled(true);
            recognizeContinuousButton.setEnabled(true);
            recognizeKwsButton.setEnabled(true);
            recognizeIntentButton.setEnabled(true);
            recognizeIntentKwsButton.setEnabled(true);
            translateButton.setEnabled(true);
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == SELECT_RECOGNIZE_LANGUAGE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String language = data.getStringExtra("language");
                languageRecognition = mapRecolanguageCode.get(language);
                recognizeLanguageTextView.setText(language);
            }
        }
        if (requestCode == SELECT_TRANSLATE_LANGUAGE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String language = data.getStringExtra("language");
                translateLanguage = mapTranlanguageCode.get(language);
                translateLanguageTextView.setText(language);
            }
        }
    }

    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    protected static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }
    public class AudioPlayingThread implements Runnable
    {
        private AudioTrack audioTrack;
        private Queue<byte[]> queue;

        public AudioPlayingThread()
        {
            audioTrack = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(16000)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build(),
                    AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
            audioTrack.play();

            queue = new LinkedBlockingQueue<byte[]>(10);
        }

        public void run()
        {
            while (true)
            {
                if (!queue.isEmpty())
                {
                    byte[] dataBlock = queue.remove();
                    audioTrack.write(dataBlock, 44, dataBlock.length - 44);
                }
            }
        }

        public void write(byte[] data)
        {
            if (queue.size() < 10) {
                queue.add(data);
            }
        }

        public void pause()
        {
            try {
                queue.clear();
                int audioState = audioTrack.getState();
                if (audioTrack != null && audioState == AudioTrack.STATE_INITIALIZED) {
                    audioTrack.pause();
                    audioTrack.flush();
                    audioTrack.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //make sure the system time is synchronized.
    public boolean checkSystemTime(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String date = simpledateformat.format(calendar.getTime());
        String year = date.substring(6,10);
        Log.i("System time" , date);
        if(Integer.valueOf(year) < 2018){
            Log.i("System time" , "Please synchronize system time");
            setTextbox("System time is " + date + "\n" +"Please synchronize system time");
            return false;
        }
        return true;
    }
}
