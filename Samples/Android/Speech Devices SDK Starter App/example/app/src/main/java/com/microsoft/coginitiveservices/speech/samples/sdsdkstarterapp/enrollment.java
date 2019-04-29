package com.microsoft.coginitiveservices.speech.samples.sdsdkstarterapp;import android.content.Intent;import android.content.res.AssetManager;import android.os.AsyncTask;import android.os.Bundle;import android.support.v7.app.AppCompatActivity;import android.support.v7.widget.Toolbar;import android.text.Layout;import android.text.TextUtils;import android.text.method.ScrollingMovementMethod;import android.util.Log;import android.view.Menu;import android.view.MenuItem;import android.view.View;import android.view.WindowManager;import android.widget.AdapterView;import android.widget.ArrayAdapter;import android.widget.Button;import android.widget.EditText;import android.widget.ListView;import android.widget.TextView;import com.microsoft.cognitiveservices.speech.ConversationTranscriber;import com.microsoft.cognitiveservices.speech.Participant;import com.microsoft.cognitiveservices.speech.SpeechConfig;import com.microsoft.cognitiveservices.speech.User;import com.microsoft.cognitiveservices.speech.audio.AudioConfig;import org.json.JSONArray;import org.json.JSONObject;import java.io.BufferedInputStream;import java.io.BufferedReader;import java.io.DataOutputStream;import java.io.File;import java.io.FileInputStream;import java.io.InputStream;import java.io.InputStreamReader;import java.math.BigInteger;import java.net.HttpURLConnection;import java.net.URI;import java.net.URL;import java.util.ArrayList;import java.util.Calendar;import java.util.HashMap;import java.util.Random;import java.util.concurrent.ExecutionException;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;import java.util.concurrent.Future;import java.util.concurrent.TimeoutException;import static com.microsoft.coginitiveservices.speech.samples.sdsdkstarterapp.LanguageCode.getCode;public class enrollment extends AppCompatActivity {    private HashMap<String, Float[]> signatureMap  = new HashMap<>();    private HashMap<String, Float[]> confAttendeeMap = new HashMap<>();    private Button enrollButton;    private Button meetingButton;    private ListView attendeeListView;    private EditText attendeeEditText;    private TextView meetingTextView;    private Toolbar meetingToolbar;    private static final String speakerRecognitionKey = "5b9e48e85daf4c32aab4d403d0eea5cd";    private String languageRecognition = new String();    private ArrayAdapter attendeeAdapter;    static final int SELECT_MEETING_RECOGNIZE_LANGUAGE_REQUEST = 0;    private SpeechConfig speechConfig = null;    private static final String inroomEndpoint = "wss://its.princetondev.customspeech.ai/speech/recognition/multiaudio";    private final ArrayList<String> rEvents = new ArrayList<>();    private final ArrayList<String> attendeeName = new ArrayList<>();    private final HashMap<String,String> attendeeVoice = new HashMap<>();    private final String logTag = "Meeting";    private boolean meetingStarted = false;    private ConversationTranscriber transcriber = null;    public boolean onCreateOptionsMenu(Menu menu){        getMenuInflater().inflate(R.menu.enrollmentmenu,menu);        return true;    }    public boolean onOptionsItemSelected(MenuItem item) {        switch (item.getItemId()) {            case R.id.RecoLanguage: {                Intent selectLanguageIntent = new Intent(this, listLanguage.class);                selectLanguageIntent.putExtra("RecognizeOrTranslate", 0);                startActivityForResult(selectLanguageIntent, SELECT_MEETING_RECOGNIZE_LANGUAGE_REQUEST);                return true;            }            case R.id.back: {                startActivity(new Intent(getApplicationContext(), MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));                return true;            }            default:                return super.onContextItemSelected(item);        }    }    @Override    protected void onCreate(Bundle savedInstanceState) {        super.onCreate(savedInstanceState);        setContentView(R.layout.activity_enrollment);        Toolbar toolbar = (Toolbar) findViewById(R.id.meetingToolbar);        enrollButton = findViewById(R.id.enrollButton);        meetingButton = findViewById(R.id.meetingButton);        attendeeListView = findViewById(R.id.attendeeListView);        attendeeEditText = findViewById(R.id.attendeeEditText);        meetingTextView = findViewById(R.id.meetingTextView);        meetingTextView.setMovementMethod(new ScrollingMovementMethod());        meetingToolbar = (Toolbar) findViewById(R.id.meetingToolbar);        setSupportActionBar(meetingToolbar);        setSupportActionBar(toolbar);        languageRecognition = "en-US";        //add attendees        double[] speakerAVoice = { 0.20341751, -0.602509737, -0.157495543, 0.235425323, -0.262581855, 0.9706452, -0.9725926, -0.5545069, -0.664979637, 0.445269465, 1.45094526, 1.14677882, -0.74700284, 0.529366255, -0.806001067, 0.600765467, -0.326947153, 0.09100193, -0.2409714, -0.794122934, 0.6553146, -0.00740871765, -1.56573009, -0.465831965, 0.8839668, -0.321104079, 0.03391044, 0.2784445, 0.335092425, 0.238702565, 0.891317964, 0.863307357, 0.3558722, 0.664056659, 1.13577628, -0.387547016, 0.863077164, -0.06684129, 0.173647225, 0.408518553, -0.5777245, 0.122216716, 0.130067632, 0.662354946, -1.75083351, 0.4670073, 1.612043, -0.040322993, 0.706456542, -0.5624599, -0.4179299, -0.664281249, 0.69724226, 0.605365634, -1.1018579, -0.6799057, 0.6062499, -0.04405103, -0.107530579, 0.167579383, -0.408960342, -0.225347459, 0.16535379, -0.701736569, 0.230436742, -1.076213, 1.16046834, -0.9302582, -1.01045966, 1.62057257, -0.78323245, -1.3208698, -0.511539757, 0.516589344, -1.23012829, -1.07660627, -0.05459263, -0.764604151, -0.599826455, 0.571749568, 0.03852504, 0.6391847, -0.003394125, 0.856304049, -1.3230567, -0.327460647, -0.9017906, -0.7030109, 0.99268043, -0.421946049, 1.75281072, 0.418413818, -0.723923266, 0.6519064, -0.3006886, 0.2822643, -0.307032257, -0.393857956, -0.5479936, 0.72080183, -0.4521299, 0.0254152175, -0.337391227, 0.8242324, 0.7695907, -0.6090447, 0.08034253, -0.477047741, 1.45401156, 0.5973801, -0.441929221, -0.353623331, -0.354328483, -0.623621941, 1.09296322, -0.852561831, -0.624510765, -0.04791019, 0.884281635, 0.7465581, -0.0980008841, 0.317451984, 0.5558065, 0.169453382, -0.7554064, -0.454769284, 0.00746863941, -0.7429495 };        double[] speakerBVoice = { 0.543933868, -0.020321995, 0.194766283, 0.4602791, 0.4087969, 1.04157507, -0.295265645, 0.388626575, -0.9917376, -0.8451343, 0.5844276, 1.00474691, 0.511118531, -0.728715837, 1.084627, 0.538570464, -0.6973523, -0.009977303, 0.494858325, -1.17110264, 0.3796776, 1.04894722, -0.20596084, 0.125631928, -0.329617023, -0.7302963, -0.814190149, 1.56026232, 0.6521595, -0.698935032, -0.102177016, 0.8477247, -0.023138985, -0.647505343, 0.142068356, -0.257996678, -0.739317656, 0.420534045, 0.673451245, -0.7069117, -0.636558354, 0.197309926, -0.5944934, 0.468094349, -0.414942026, 0.161684126, -0.14972721, -0.4831576, 0.307493448, -0.7745579, -1.09842861, 0.4889769, -0.174236149, -0.310639471, 0.114823535, 0.0189673752, -0.0393964574, 0.0509197451, -0.3160209, 0.740893245, -0.8625835, 1.053011, 0.9750041, 0.0386843272, 0.6692332, 0.418619037, 0.7506037, -0.307305753, -0.374221265, 0.143276885, -0.510534942, -0.108607173, 0.132410973, -0.0101992339, -0.3660348, -0.94513905, -0.413134933, 0.2179726, -0.191411525, -0.189982861, -0.3508947, 0.46922484, 0.224954739, -0.664483964, 0.439492673, -0.181673378, -1.07951689, -0.222088665, -0.216860339, 0.2919803, 0.764483154, -1.12793863, -0.9462612, 0.2251329, -0.7363071, 0.570853233, -0.15759638, -0.00617103837, -0.0362417921, -0.167855039, 0.457206368, -0.667343259, -0.561827242, -0.539345145, 0.0950814039, 0.3873228, -0.7935858, -0.5428926, 0.6511848, 0.6059286, 0.282933861, -0.3079573, 0.615977168, 0.2973074, -0.189168558, -0.1712849, -0.315360129, 0.432943523, 0.4326892, -0.517891943, -0.0988010541, 0.0530028343, -0.2354432, 1.61555624, -0.25957638, -1.19088089, 0.8944042, -0.994750857 };        double[] speakerCVoice = { 0.5624315, 0.9420155, -0.277492762, -0.8941554, -0.011484175, 0.415792376, 1.03119707, -0.552991331, 0.6820038, -0.484386355, 0.8350378, -0.103652582, -0.312757641, -0.7313983, -0.360428631, 1.27196527, 0.1690859, -0.545182347, 0.177536383, 0.178592056, -0.6696219, -0.2937006, -0.8765528, 1.47014141, -0.0114781708, 0.800700068, -0.213516191, 0.510062, 0.809165, 0.7471087, 0.0385493264, -0.8795135, -1.543675, 0.4684407, 0.393841684, -0.0153821819, 0.210640281, -0.626572, -0.474659264, 0.443567872, -0.6933459, -0.8407717, 1.65278053, 0.143059328, -0.312433183, -0.432634145, -0.229527891, 0.009457957, 1.22062576, -0.25846222, 0.9022701, -0.09025754, 0.759005249, -0.542685449, 0.6825161, -0.09030664, -1.32050765, 0.0810169652, -0.22590512, 1.34984732, 1.42101145, -0.6919955, -0.05281818, 0.1516853, 0.378346175, -1.45597589, 1.86006808, -1.09358954, 0.164541557, 1.15899038, 0.400304556, -1.4556433, -0.6491593, 0.3153955, -1.02359319, 0.224946812, -0.766106069, 0.773537338, 0.3917014, -0.650540948, -1.32942283, 0.337933, 0.09205164, -0.154064924, 1.00586629, 0.8625995, 0.261413842, -1.77852392, 0.700665832, -0.509795, -0.608610749, -0.133641, -0.07755462, -0.263543159, 1.014122, 0.059133783, 0.190727144, 1.14281893, -0.1855065, 0.8167669, 1.99718475, -0.138079941, 0.104927227, 1.0870564, -0.374583244, -0.6522629, 0.175296053, 0.235443637, 0.142983943, 0.296690881, -0.493942976, 0.226852983, 0.131097317, 0.436143667, 0.0747649744, 0.154153034, 0.18105334, -0.384421766, 0.287202775, 1.15747261, -0.562260032, 0.4153471, 0.564605, 0.404231369, 0.5500285, 0.5841256, 0.5638335, -0.5350347 };        double[] speakerDVoice = { 0.0321208239, 0.3227103, -0.670678258, -0.9601845, 1.26203167, 0.129518911, -0.9184133, 0.788482666, -0.6394711, -0.5586596, 0.3032503, 0.205323413, 0.6066487, 1.12603712, 0.9768588, 0.7452101, 0.04399666, -0.492016077, 0.09767708, -1.33410549, 0.3083104, -0.7253642, 1.88533378, -1.57200122, 0.345329463, 0.03210179, -0.08656986, -0.04080937, 0.195850626, -0.06724851, 0.493156165, 1.4327805, 0.719427466, 0.88552177, -0.150471464, -0.0471941233, 1.60217154, -0.12176612, -0.864559233, 0.541845441, 0.2549711, -0.330598176, 0.503128, -0.126131028, 0.848801851, -0.9465101, 0.214955747, -0.267756373, 0.272672176, 0.718044043, 0.135466173, 0.687997162, 0.3311102, -0.241822436, -0.8230289, 0.2918076, -0.09779839, 1.11676526, 0.266681, -0.81734097, -0.532646537, 0.294002354, 0.282385528, 0.706647158, -0.201713413, -0.591578245, 0.5480252, 1.08896577, -0.122984774, -0.478253245, 0.786480546, -0.115511119, 0.129337028, -0.791936159, 0.690756857, 1.6754837, -0.11298576, 1.73266172, -0.6431971, 0.488045782, -0.992822766, 0.119650088, -0.5923358, 0.005940523, -0.181966037, 0.700773239, 0.0316376761, 0.549139261, -0.289723545, -0.6089702, -0.7352804, -0.276392519, -1.10303235, -0.000194538385, -0.247708842, -0.268450916, 0.853501439, 0.08953567, -0.2218205, -0.03386849, -0.583519161, -1.05726957, 0.462729871, 0.451614738, 0.6828421, -0.3481504, 0.06270793, 0.4052988, 1.350086, 0.268068671, -0.307869, -1.8097266, 0.406426132, 0.425236583, -0.00133464672, 0.3816143, 0.687562644, 0.11974448, 0.008705301, -0.462561548, -0.0509029329, 0.3599257, -0.477088034, 0.8096022, -0.4347555, 1.02042329, 0.6471451, -1.02284336 };        confAttendeeMap.put("A", toFloatArray(speakerAVoice));        confAttendeeMap.put("B", toFloatArray(speakerBVoice));        confAttendeeMap.put("C", toFloatArray(speakerCVoice));        confAttendeeMap.put("D", toFloatArray(speakerDVoice));        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);       // attendeeAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, attendeeName);        attendeeAdapter = new ArrayAdapter(this,R.layout.text_size,attendeeName);        attendeeListView.setAdapter(attendeeAdapter);        attendeeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {            @Override            public void onItemClick(AdapterView<?> parent, final View view,                                    int position, long id) {                final String item = (String) parent.getItemAtPosition(position);                attendeeName.remove(position);                signatureMap.remove(item);                attendeeAdapter.notifyDataSetChanged();                //setRecognizedText("remove " + item);            }        });        enrollButton.setOnClickListener(view ->{            enrollButton.setEnabled(false);            clearTextBox();            String aName =attendeeEditText.getText().toString();            if(aName.length() == 0){                setRecognizedText("Please input attendee's name");                enrollButton.setEnabled(true);            }else{                /*                File wavFile = new File(aName);                if(wavFile.exists()) {                    String userId = wavFile.getName();                    new enrollTask().execute(userId.substring(0, userId.length()-4), fileName);                }else{                    setRecognizedText(fileName + " does not exist");                }                */                if (confAttendeeMap.containsKey(aName.trim()) && !signatureMap.containsKey(aName.trim())) {                    attendeeEditText.setText("");                    attendeeName.add(aName.trim());                    signatureMap.put(aName.trim() , confAttendeeMap.get(aName.trim()));                    attendeeAdapter.notifyDataSetChanged();                    enrollButton.setEnabled(true);                } else if(signatureMap.containsKey(aName.trim())){                        setRecognizedText("The attendee's name is already in attendees' list" + "\n" + "\n");                        enrollButton.setEnabled(true);                    } else{                        setRecognizedText("The attendee's name is not in configuration list" + "\n" + "\n");                        enrollButton.setEnabled(true);                }            }        });        meetingButton.setOnClickListener(view ->{            ///////////////////////////////////////////////////            // check if we have a valid key            ///////////////////////////////////////////////////            disableButtons();            clearTextBox();            if (meetingStarted) {                stopClicked();                meetingStarted = false;                return;            }            clearTextBox();            if (!MainActivity.checkSpeechKey()) {                setRecognizedText("Warning: Please update SpeechSubscriptionKey with your actual subscription key!");                return;            }            if(signatureMap.size() == 0){                setRecognizedText("Please add the meeting attendee first!");                return;            }else{                //speechConfig = SpeechConfig.fromEndpoint(URI.create(inroomEndpoint), "e6f600667f394bb4a9a461447ee9a0e8");                speechConfig = SpeechConfig.fromEndpoint(URI.create(inroomEndpoint),"5b9e48e85daf4c32aab4d403d0eea5cd");                speechConfig.setProperty("DeviceGeometry", "Circular6+1");                speechConfig.setProperty("SelectedGeometry", "Raw");                try {                    AssetManager assetManager = getAssets();                    transcriber = new ConversationTranscriber(speechConfig, /*AudioConfig.fromStreamInput(ais)*/AudioConfig.fromDefaultMicrophoneInput());                    transcriber.setConversationId("MeetingTest");                    Log.i(logTag, "Participants enrollment");                    // add by user Id                    for(String userId:signatureMap.keySet()){                        User user = User.fromUserId(userId);                        transcriber.addParticipant(user);                        Float image[] = { 3.3f, 4.4f };                        Participant participant = Participant.from(userId, "en-US", signatureMap.get(userId));                        transcriber.addParticipant(participant);                        Log.i(logTag," Add attendee: " + userId);                    }                    startRecognizeMeeting(transcriber);                    meetingStarted = true;                }catch(Exception ex){                    System.out.println(ex.getMessage());                    displayException(ex);                }            }        });    }    private void startRecognizeMeeting(ConversationTranscriber t) throws InterruptedException, ExecutionException, TimeoutException {        try {            t.recognizing.addEventListener((o, e) -> {                final String text = e.getResult().getText();                final String speakerId = e.getResult().getSpeakerId();                final String result = speakerId + " : " +text;                Log.i(logTag, "Intermediate result received: " + result);                rEvents.add(result);                setRecognizedText(TextUtils.join("\n", rEvents));                rEvents.remove(rEvents.size() - 1);            });            t.recognized.addEventListener((o, e) -> {                final String text = e.getResult().getText();                final String speakerId = e.getResult().getSpeakerId();                final String result =speakerId + " : " +text;                long currentTime = Calendar.getInstance().getTimeInMillis();                Log.i(logTag, "Final result received: " + Long.toString(currentTime)+"  "+ result);                rEvents.add(result);                setRecognizedText(TextUtils.join("\n", rEvents));            });            rEvents.clear();            final Future<Void> task = t.startTranscribingAsync();            setOnTaskCompletedListener(task, result -> {                long currentTime = Calendar.getInstance().getTimeInMillis();                Log.i(logTag, "Recognition started. " + Long.toString(currentTime));                enrollment.this.runOnUiThread(() -> {                    enableButtons("Stop");                });            });        }catch (Exception ex){            System.out.println(ex.getMessage());            displayException(ex);        }    }    private void stopClicked(){        try {            final Future<Void> task = transcriber.stopTranscribingAsync();            setOnTaskCompletedListener(task, result -> {                Log.i(logTag, "Recognition stopped.");                enrollment.this.runOnUiThread(() -> {                    enableButtons("Meeting Start");                });            });        }        catch (Exception ex) {            System.out.println(ex.getMessage());            displayException(ex);        }    }    private void clearTextBox() {        appendTextLine("", true);    }    private void setRecognizedText(final String s) {        appendTextLine(s, true);    }    private void appendTextLine(final String s, final Boolean erase) {        enrollment.this.runOnUiThread(() -> {            if (erase) {                meetingTextView.setText(s);            } else {                String txt = meetingTextView.getText().toString();                meetingTextView.setText(txt + "\n" + s);            }            final Layout layout =meetingTextView.getLayout();            if(layout != null) {                int scrollDelta = layout.getLineBottom(meetingTextView.getLineCount() - 1)                        - meetingTextView.getScrollY() - meetingTextView.getHeight();                if (scrollDelta > 0)                    meetingTextView.scrollBy(0, scrollDelta);            }        });    }    protected void onActivityResult(int requestCode, int resultCode, Intent data) {        // Check which request we're responding to        if (requestCode == SELECT_MEETING_RECOGNIZE_LANGUAGE_REQUEST) {            // Make sure the request was successful            if (resultCode == RESULT_OK) {                String language = data.getStringExtra("language");                speechConfig.setSpeechRecognitionLanguage( getCode(0,language));                Log.i(logTag,language +" Recognizing");            }        }    }    private void disableButtons() {        enrollment.this.runOnUiThread(() -> {            meetingButton.setEnabled(false);        });    }    public void enableButtons(String text) {        enrollment.this.runOnUiThread(() -> {            meetingButton.setText(text);            meetingButton.setEnabled(true);        });    }    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {        s_executorService.submit(() -> {            T result = task.get();            listener.onCompleted(result);            return null;        });    }    private interface OnTaskCompletedListener<T> {        void onCompleted(T taskResult);    }    protected static ExecutorService s_executorService;    static {        s_executorService = Executors.newCachedThreadPool();    }    private void displayException(Exception ex) {        meetingTextView.setText(ex.getMessage() + "\n" + TextUtils.join("\n", ex.getStackTrace()));    }    public class enrollTask extends AsyncTask<String, String, String> {        Float[] signatureId;        @Override        protected String doInBackground(String[] params) {            String enrollId = params[0];            try{                String lineEnd = "\r\n";                String twoHyphens = "--";                String boundary = new BigInteger(256, new Random()).toString();                File wavFile = new File(params[1]);                Log.i("Meeting", "enroll: " + enrollId + " audio file: " + wavFile.getName());                int bytesRead, bytesAvailable, bufferSize;                byte[] buffer;                int maxBufferSize = 1024 * 1024;                URL url = new URL("https://pss.princetondev.customspeech.ai/api/v1/Signature/GenerateVoiceSignatureFromFile");                HttpURLConnection connection = (HttpURLConnection) url.openConnection();                // Allow Inputs &amp; Outputs.                connection.setDoInput(true);                connection.setDoOutput(true);                connection.setUseCaches(false);                // Set HTTP method to POST.                connection.setRequestMethod("POST");                connection.setRequestProperty("accept", "application/json");                connection.setRequestProperty("Ocp-Apim-Subscription-Key", speakerRecognitionKey);                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);                FileInputStream fileInputStream;                DataOutputStream outputStream;                outputStream = new DataOutputStream(connection.getOutputStream());                outputStream.writeBytes(twoHyphens + boundary + lineEnd);                outputStream.writeBytes("Content-Disposition: form-data; name=\"File\";filename=\"" + wavFile.getName() +"\"" + lineEnd);                outputStream.writeBytes(lineEnd);                fileInputStream = new FileInputStream(wavFile);                bytesAvailable = fileInputStream.available();                bufferSize = Math.min(bytesAvailable, maxBufferSize);                Log.i("Meeting", "Buffer size: " + Integer.toString(bufferSize));                buffer = new byte[bufferSize];                // Read file                bytesRead = fileInputStream.read(buffer, 0, bufferSize);                int count = 1;                while (bytesRead > 0) {                    outputStream.write(buffer, 0, bufferSize);                    bytesAvailable = fileInputStream.available();                    bufferSize = Math.min(bytesAvailable, maxBufferSize);                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);                }                outputStream.writeBytes("\r\n--" + boundary + "--");                // Responses from the server (code and message)                int serverResponseCode = connection.getResponseCode();                Log.i("Meeting", "serverResponseCode: " + Integer.toString(serverResponseCode));                String result = null;                if (serverResponseCode == 200) {                    StringBuilder s_buffer = new StringBuilder();                    InputStream is = new BufferedInputStream(connection.getInputStream());                    BufferedReader br = new BufferedReader(new InputStreamReader(is));                    String inputLine;                    while ((inputLine = br.readLine()) != null) {                        s_buffer.append(inputLine);                    }                    result = s_buffer.toString();                    Log.i("Meeting", "Response result: " + result);                    if (result != null) {                        JSONObject signature = new JSONObject(result);                        String status = signature.getString("Status");                        if(status.equals("OK")){                            Log.i("Meeting","Enrollment: Get Signature ID from GenerateVoiceSignatureFromFile is OK");                            JSONObject sr = signature.getJSONObject("Signature");                            JSONArray signaureArray = sr.optJSONArray("Signature");                            signatureId = new Float[signaureArray.length()];                            for(int i = 0; i< signaureArray.length(); i++){                                signatureId[i] =(float)signaureArray.getDouble(i);                            }                            signatureMap.put(enrollId, signatureId);                        }                    }                }                fileInputStream.close();                outputStream.flush();                outputStream.close();                Log.i("Meeting", "Enrollment is finished: " + enrollId);            } catch (Exception e) {                e.printStackTrace();            }            return enrollId;        }        @Override        protected void onPostExecute(String result) {            if(result.length() != 0)            {                setRecognizedText("Enrollment1 is successful");                attendeeEditText.setText("");                attendeeName.add(result);                attendeeAdapter.notifyDataSetChanged();                enrollButton.setEnabled(true);            }        }    }    Float[] toFloatArray(double[] arr) {        if (arr == null) return null;        int n = arr.length;        Float[] ret = new Float[n];        for (int i = 0; i < n; i++) {            ret[i] = (float)arr[i];        }        return ret;    }}