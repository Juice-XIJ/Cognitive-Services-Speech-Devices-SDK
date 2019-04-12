package com.microsoft.coginitiveservices.speech.samples.sdsdkstarterapp;import android.content.Intent;import android.os.AsyncTask;import android.os.Bundle;import android.support.v7.app.AppCompatActivity;import android.support.v7.widget.Toolbar;import android.text.TextUtils;import android.text.method.ScrollingMovementMethod;import android.util.Log;import android.view.Menu;import android.view.MenuItem;import android.widget.Button;import android.widget.TextView;import com.microsoft.cognitiveservices.speech.ConversationTranscriber;import com.microsoft.cognitiveservices.speech.Participant;import com.microsoft.cognitiveservices.speech.SpeechConfig;import com.microsoft.cognitiveservices.speech.User;import com.microsoft.cognitiveservices.speech.audio.AudioConfig;import org.json.JSONArray;import org.json.JSONObject;import java.io.BufferedInputStream;import java.io.BufferedReader;import java.io.DataOutputStream;import java.io.File;import java.io.FileInputStream;import java.io.InputStream;import java.io.InputStreamReader;import java.math.BigInteger;import java.net.HttpURLConnection;import java.net.URI;import java.net.URL;import java.util.ArrayList;import java.util.HashMap;import java.util.Random;import java.util.concurrent.ExecutionException;import java.util.concurrent.Future;import java.util.concurrent.TimeUnit;import java.util.concurrent.TimeoutException;import static com.microsoft.coginitiveservices.speech.samples.sdsdkstarterapp.LanguageCode.getCode;public class enrollment extends AppCompatActivity {    private HashMap<String, Float[]> signatureMap = new HashMap<>();    private Button enrollButton1;    private Button enrollButton2;    private Button enrollButton3;    private Button meetingButton;    private TextView enrollTextView1;    private TextView enrollTextView2;    private TextView enrollTextView3;    private TextView meetingTextView;    private Toolbar meetingToolbar;    private TextView recoLanguageTextView;    private static final String speakerRecognitionKey = "5b9e48e85daf4c32aab4d403d0eea5cd";    private String languageRecognition = new String();    static final int SELECT_MEETING_RECOGNIZE_LANGUAGE_REQUEST = 0;    private SpeechConfig speechConfig;    private static final String inroomEndpoint = "wss://bvt-pfe.demo1.princeton.customspeech.ai/speech/recognition/princeton?";    private static final String MeetingAudioInput= "/video/DictationBatman.wav";    private final ArrayList<String> rEvents = new ArrayList<>();    private final String logTag = "Meeting";    public boolean onCreateOptionsMenu(Menu menu){        getMenuInflater().inflate(R.menu.enrollmentmenu,menu);        return true;    }    public boolean onOptionsItemSelected(MenuItem item) {        switch (item.getItemId()) {            case R.id.RecoLanguage: {                Intent selectLanguageIntent = new Intent(this, listLanguage.class);                selectLanguageIntent.putExtra("RecognizeOrTranslate", 0);                startActivityForResult(selectLanguageIntent, SELECT_MEETING_RECOGNIZE_LANGUAGE_REQUEST);                return true;            }            case R.id.back: {                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));                return true;            }            default:                return super.onContextItemSelected(item);        }    }    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_enrollment);        Toolbar toolbar = (Toolbar) findViewById(R.id.meetingToolbar);        enrollButton1 = findViewById(R.id.enrollButton1);        enrollButton2 = findViewById(R.id.enrollButton2);        enrollButton3 = findViewById(R.id.enrollButton3);        meetingButton = findViewById(R.id.meetingButton);        enrollTextView1 = findViewById(R.id.enrollTextView1);        enrollTextView2 = findViewById(R.id.enrollTextView2);        enrollTextView3 = findViewById(R.id.enrollTextView3);        meetingTextView = findViewById(R.id.meetingTextView);        meetingTextView.setMovementMethod(new ScrollingMovementMethod());        recoLanguageTextView = findViewById(R.id.recoLanguageTextView);        enrollTextView1.setText("");        enrollTextView2.setText("");        enrollTextView3.setText("");        meetingToolbar = (Toolbar) findViewById(R.id.meetingToolbar);        setSupportActionBar(meetingToolbar);        setSupportActionBar(toolbar);        languageRecognition = "en-US";        recoLanguageTextView.setText("English Recognizing");        enrollButton1.setOnClickListener(view ->{            enrollButton1.setEnabled(false);            new enrollTask().execute("One","/video/DictationBatman.wav");        });        enrollButton2.setOnClickListener(view ->{            enrollButton2.setEnabled(false);            new enrollTask().execute("Two","/video/a.wav");        });        enrollButton3.setOnClickListener(view ->{            enrollButton3.setEnabled(false);            new enrollTask().execute("Three","/video/jingli2.wav");        });        meetingButton.setOnClickListener(view ->{            ///////////////////////////////////////////////////            // check if we have a valid key            ///////////////////////////////////////////////////            if (!MainActivity.checkSpeechKey()) {                meetingTextView.setText("Warning: Please update SpeechSubscriptionKey with your actual subscription key!");                return;            }            if(signatureMap.size() == 0){                meetingTextView.setText("Please enroll the meeting attendee first!");            }else{                //speechConfig = MainActivity.getSpeechConfig();                SpeechConfig s = SpeechConfig.fromEndpoint(URI.create(inroomEndpoint), MainActivity.getSpeechRegion());                if(new File(MeetingAudioInput).exists()) {                    try {                        WavFileAudioInputStream ais = new WavFileAudioInputStream(MeetingAudioInput);                        ConversationTranscriber t = new ConversationTranscriber(s, AudioConfig.fromStreamInput(ais));                        t.setConversationId("MeetingTest");                        // add by user Id                        for(String userId:signatureMap.keySet()){                            User user = User.fromUserId(userId);                            t.addParticipant(user);                            Float image[] = { 3.3f, 4.4f };                            //Log.i(logTag, "Speech Recognition Language " + speechConfig.getSpeechRecognitionLanguage());                            Participant participant = Participant.from(userId, "en-US", signatureMap.get(userId), image);                            t.addParticipant(participant);                        }                        meetingTextView.setText("");                        startRecognizeMeeting(t);                        t.close();                        s.close();                    }catch(Exception ex){                        System.out.println(ex.getMessage());                        displayException(ex);                    }                } else{                    return;                }            }        });    }    private void startRecognizeMeeting(ConversationTranscriber t) throws InterruptedException, ExecutionException, TimeoutException {        try {            t.recognizing.addEventListener((o, e) -> {                Log.i(logTag,"conversation transcriber recognizing:" + e.toString());            });            t.recognized.addEventListener((o, e) -> {                rEvents.add(e.getResult().getText());                Log.i(logTag,"conversation transcriber recognized:" + e.toString());                StringBuilder res = new StringBuilder();                for(String result:rEvents) {                    res.append(result);                    res.append("\n");                }                meetingTextView.setText(res.toString());            });            Future<?> future = t.startTranscribingAsync();            // Wait for max 30 seconds            future.get(30, TimeUnit.SECONDS);            // wait until we get at least on final result            long now = System.currentTimeMillis();            while (((System.currentTimeMillis() - now) < 30000) &&                    (rEvents.isEmpty())) {                Thread.sleep(200);            }            future = t.stopTranscribingAsync();            // Wait for max 30 seconds            future.get(30, TimeUnit.SECONDS);        }catch (Exception ex){            System.out.println(ex.getMessage());            displayException(ex);        }    }    public class enrollTask extends AsyncTask<String, String, String> {        Float[] signatureId;        @Override        protected String doInBackground(String[] params) {            String enrollId = params[0];            try{                String lineEnd = "\r\n";                String twoHyphens = "--";                String boundary = new BigInteger(256, new Random()).toString();                File wavFile = new File(params[1]);                Log.i("Meeting", "enroll: " + enrollId + " audio file: " + wavFile.getName());                int bytesRead, bytesAvailable, bufferSize;                byte[] buffer;                int maxBufferSize = 1024 * 1024;                URL url = new URL("https://pss.princetondev.customspeech.ai/api/v1/Signature/GenerateVoiceSignatureFromFile");                HttpURLConnection connection = (HttpURLConnection) url.openConnection();                // Allow Inputs &amp; Outputs.                connection.setDoInput(true);                connection.setDoOutput(true);                connection.setUseCaches(false);                // Set HTTP method to POST.                connection.setRequestMethod("POST");                connection.setRequestProperty("accept", "application/json");                connection.setRequestProperty("Ocp-Apim-Subscription-Key", speakerRecognitionKey);                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);                FileInputStream fileInputStream;                DataOutputStream outputStream;                outputStream = new DataOutputStream(connection.getOutputStream());                outputStream.writeBytes(twoHyphens + boundary + lineEnd);                outputStream.writeBytes("Content-Disposition: form-data; name=\"File\";filename=\"" + wavFile.getName() +"\"" + lineEnd);                outputStream.writeBytes(lineEnd);                fileInputStream = new FileInputStream(wavFile);                bytesAvailable = fileInputStream.available();                bufferSize = Math.min(bytesAvailable, maxBufferSize);                Log.i("Meeting", "Buffer size: " + Integer.toString(bufferSize));                buffer = new byte[bufferSize];                // Read file                bytesRead = fileInputStream.read(buffer, 0, bufferSize);                int count = 1;                while (bytesRead > 0) {                    outputStream.write(buffer, 0, bufferSize);                    bytesAvailable = fileInputStream.available();                    bufferSize = Math.min(bytesAvailable, maxBufferSize);                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);                }                outputStream.writeBytes("\r\n--" + boundary + "--");                // Responses from the server (code and message)                int serverResponseCode = connection.getResponseCode();                Log.i("Meeting", "serverResponseCode: " + Integer.toString(serverResponseCode));                String result = null;                if (serverResponseCode == 200) {                    StringBuilder s_buffer = new StringBuilder();                    InputStream is = new BufferedInputStream(connection.getInputStream());                    BufferedReader br = new BufferedReader(new InputStreamReader(is));                    String inputLine;                    while ((inputLine = br.readLine()) != null) {                        s_buffer.append(inputLine);                    }                    result = s_buffer.toString();                    Log.i("Meeting", "Response result: " + result);                    if (result != null) {                        JSONObject signature = new JSONObject(result);                        String status = signature.getString("Status");                        if(status.equals("OK")){                            Log.i("Meeting","Enrollment: Get Signature ID from GenerateVoiceSignatureFromFile is OK");                            JSONObject sr = signature.getJSONObject("Signature");                            JSONArray signaureArray = sr.optJSONArray("Signature");                            signatureId = new Float[signaureArray.length()];                            for(int i = 0; i< signaureArray.length(); i++){                                 signatureId[i] =(float)signaureArray.getDouble(i);                            }                            signatureMap.put(enrollId, signatureId);                        }                    }                }                fileInputStream.close();                outputStream.flush();                outputStream.close();                Log.i("Meeting", "Enrollment is finished: " + enrollId);            } catch (Exception e) {                e.printStackTrace();            }            return enrollId;        }        @Override        protected void onPostExecute(String result) {            switch(result){                case "One" : {                    enrollTextView1.setText("Enrollment1 is successful");                    enrollButton1.setEnabled(true);                    break;                }                case "Two" : {                    enrollTextView2.setText("Enrollment2 is successful");                    enrollButton2.setEnabled(true);                    break;                }                case "Three" :  {                    enrollTextView3.setText("Enrollment3 is successful");                    enrollButton3.setEnabled(true);                    break;                }                default:            }        }    }    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        // Check which request we're responding to        if (requestCode == SELECT_MEETING_RECOGNIZE_LANGUAGE_REQUEST) {            // Make sure the request was successful            if (resultCode == RESULT_OK) {                String language = data.getStringExtra("language");                speechConfig.setSpeechRecognitionLanguage( getCode(0,language));                recoLanguageTextView.setText(language +" Recognizing");            }        }    }    private void displayException(Exception ex) {        meetingTextView.setText(ex.getMessage() + "\n" + TextUtils.join("\n", ex.getStackTrace()));    }}