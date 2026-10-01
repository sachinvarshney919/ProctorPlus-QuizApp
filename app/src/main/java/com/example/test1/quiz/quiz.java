package com.example.test1.quiz;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.test1.MainActivity;
import com.example.test1.R;
import com.example.test1.databinder.holdername;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class quiz extends AppCompatActivity {
    FirebaseFirestore db; FirebaseAuth auth;  FirebaseUser user;
    String useremail;
    String choosen;
//    ProgressBar progress= findViewById(R.id.progress);
    Button next,prev;
    String usercode;String userpin;
    TextView question,test,a,b,c,d,count,subname;
    RadioButton optiona; RadioButton optionb; RadioButton optionc;  RadioButton optiond;
    String author,subject,time,date;String timestamp;
    String strsubname;
    String duration;
    Button submit,finish;

    int i=0; Map<Integer,String> map;
    int marks=0;Map<Integer,Integer> marksdata= new HashMap<>();
    String numdata,email;
    int numques;
    String maxmarks="null";
    String formattedTimestamp;
    TextView timer; //
    CountDownTimer countDownTimer;
    FrameLayout loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //performing quiz

        prev=findViewById(R.id.prev);
        submit=findViewById(R.id.submit);
        question=findViewById(R.id.question);
        next=findViewById(R.id.next);
        subname= findViewById(R.id.subname);
        loading=findViewById(R.id.loading);
//       5

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        useremail=user.getEmail();
        timer = findViewById(R.id.timer);
        finish=findViewById(R.id.finish);
        count= findViewById(R.id.count);



        Intent intent= getIntent();
        usercode=  intent.getStringExtra("code");
        userpin=  intent.getStringExtra("pin");
//        timestamp = intent.getStringExtra("timestamp");
        db= FirebaseFirestore.getInstance();


        otherdata(usercode,userpin);

        optiona=findViewById(R.id.ra);
        optionb=findViewById(R.id.rb);
        optionc=findViewById(R.id.rc);
        optiond=findViewById(R.id.rd);
        a=findViewById(R.id.a);
        b=findViewById(R.id.b);
        c=findViewById(R.id.c);
        d=findViewById(R.id.d);
        map = new HashMap<>();
        optiona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setback();
                optiona.setChecked(true);
            }
        });
        optionb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setback();
                optionb.setChecked(true);
            }
        });
        optionc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setback();
                optionc.setChecked(true);
            }
        });
        optiond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setback();
                optiond.setChecked(true);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextques(usercode,userpin,"next");
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextques(usercode,userpin,"prev");
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(optiona.isChecked()){
                    choosen = a.getText().toString();
                }
                else if(optionb.isChecked()){
                    choosen = b.getText().toString();

                }
                else if(optionc.isChecked()){
                    choosen = c.getText().toString();
                }
                else if(optiond.isChecked()){
                    choosen = d.getText().toString();
                }
                else{
                    Toast.makeText(quiz.this, "Please check any answer", Toast.LENGTH_SHORT).show();
                    return;
                }
                submit.setText("Submitted");
                map.put(i,"yes");
                checkanswer(choosen,usercode,userpin,useremail);
                personalchoice(choosen,usercode,userpin,useremail);
            }
        });
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish.setEnabled(false);
                submit.performClick();
                sendmarks(usercode,userpin);
                datatopersonal("","","","","finish");
                sendmarkstopersonal(usercode,userpin);
                storepersonaldatatoauthor(usercode,userpin,useremail);
                sendfinish(usercode,userpin,"ff");
                Intent intent1 = new Intent(quiz.this, quizperformcompleted.class);
                startActivity(intent1);
                finish();
            }
        });

    }

    public void otherdata(String code,String pin){//Details of QUiz
        db.collection("data").document("quiz").collection(code + pin).document("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                numdata = document.getString("number");
                                timestamp  = document.getString("schedule");
                                duration = document.getString("duration").toString();
                                numques=Integer.parseInt(numdata);
                                maxmarks=document.getString("maxmark");
                                strsubname= document.getString("subject");
                                subname.setText(strsubname);
                                sendinfo(usercode,userpin);
                                startTimer(duration);

                                count.setText(i+" "+"Of "+numques);
                                sendfinish(usercode,userpin,"");
                                nextques(code,pin,"");

                            } else {
                                Log.d(TAG, "No such document.");
                            }
                        } else {
                            Log.w(TAG, "Error getting document.", task.getException());
                        }
                    }
                });
    }
    public void nextques(String usercode, String userpin,String ref) {
        if(ref.equals("prev")){
            i--;
        }
        else {
            i++;
        }

        count.setText(String.valueOf(i)+" "+"of"+" "+String.valueOf(numques));
        if(i==numques){
            next.setVisibility(GONE);
        }
        if(i<numques){
            next.setVisibility(View.VISIBLE);
        }
        if(i==1){
            prev.setVisibility(GONE);
        }
        if(i>1) {
            prev.setVisibility(View.VISIBLE);
        }

        db.collection("data").document("quiz").collection(usercode + userpin).document(String.valueOf(i))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String questionText = document.getString("question");
                                loading.setVisibility(GONE);
                                question.setText(questionText);
                                a.setText(document.getString("a"));
                                b.setText(document.getString("b"));
                                c.setText(document.getString("c"));
                                d.setText(document.getString("d"));
                                if(map.containsKey(i)&&map.get(i).equals("yes")){
                                    submit.setText("submitted");
                                    chechpreviousmarked(i);
                                }
                                else{
                                    submit.setText("Submit");
                                    setback();

                                }
                            } else {
                                i--;
                                next.setVisibility(GONE);
                            }
                        } else {
                            Log.w(TAG, "Error getting document.", task.getException());
                        }
                    }
                });

    }
    //
    public void personalchoice(String choice,String usercode,String userpin,String email){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user1 = auth.getCurrentUser();
        email = user1.getEmail().toString();
        Map<String, Object> user = new HashMap<>();
        user.put(String.valueOf(i), choice);

        db.collection("personal").document(email).collection("attempted").document(timestamp)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Document successfully merged in storedatatopersonal()");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
//    public void storedatatoauthor(String choice,String usercode,String userpin,String email){
//        DocumentReference docRef = db.collection("data").document("quiz").collection(usercode+userpin).document("participant").collection(email).document(String.valueOf(i));
//        Map<String, Object> user = new HashMap<>();
//        user.put("choice", choice);
//        docRef.set(user)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "DocumentSnapshot successfully written!");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error writing document", e);
//                    }
//                });
//    }

    //STORING DATA
    public void storepersonaldatatoauthor(String code,String pin,String email){
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("marks", String.valueOf(marks));
        user.put("name", holdername.getInstance().getData());

        db.collection("data").document("quiz").collection(code+pin).document("participant").collection("data").document(useremail)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    public void setback(){
        optiona.setChecked(false);
        optionb.setChecked(false);
        optionc.setChecked(false);
        optiond.setChecked(false);
    }

    //GETTING QUIZ DATA
    public void sendinfo(String code,String pin){
        db.collection("data").document("quiz").collection(code + pin).document("data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                author=document.getString("author");
                                subject=document.getString("subject");
                                time= document.getString("time");
                                date=document.getString("date");
                                datatopersonal(author,subject,time,date,"");

                            } else {

                            }
                        } else {
                            Log.w(TAG, "Error getting document.", task.getException());
                        }
                    }
                });

    }

    public void checkanswer(String choosen,String code,String pin,String email){
        db.collection("data").document("quiz").collection(code + pin).document(String.valueOf(i))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String selected= task.getResult().getString("selected");
                        if(selected.equals(choosen)){
                            if(marksdata.containsKey(i)){
                                int mar =   marksdata.get(i);
                                if(mar==1){
                                    marks--;
                                    marksdata.put(i,0);
                                }
                            }
                            marks++;
                            marksdata.put(i,1);
                        }

                    }
                });

    }
    public void datatopersonal(String author,String subject,String time,String date,String ref){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user1 = auth.getCurrentUser();
        String email= user1.getEmail().toString();

        Map<String,Object> user = new HashMap<>();
        if(ref.equals("")){ user.put("author",author);
            user.put("subject",subject);
            user.put("time",time);
            user.put("date",date);
            user.put("maxmark",maxmarks);
        }
        else{
            user.put("marks",String.valueOf(marks));
        }

        db.collection("personal").document(email).collection("attempted").document(timestamp)
                .set(user,SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                    }
                });
    }
    public void sendmarkstopersonal(String usercode,String userpin){
//        String strmarks  =String.valueOf(marks);
//        Map<String,Object> user1= new HashMap<>();
//        user1.put("marks",strmarks);
//        db.collection("personal").document(useremail).collection("attempted").document(timestamp)
//                .set(user1)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//
//                    }
//                });

    }

    //FOR SAVING MARKS
    public void sendmarks(String code,String pin){
        String strmarks  =String.valueOf(marks);

        Map<String, Object> user = new HashMap<>();

        user.put("marks", strmarks);
        user.put("status","yes");
        db.collection("data").document("quiz").collection(code+pin).document("participant").collection("data").document(useremail)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {


                    }
                });
    }

    public  void sendfinish(String code,String pin,String ref){

        Map<String, Object> user = new HashMap<>();
        if(ref.equals("")){
            user.put("status", "no");

        }
        else {
            user.put("status", "completed");
        }

        db.collection("personal").document(useremail).collection(code+pin).document("attempted").collection(timestamp)
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Toast.makeText(quiz.this, "yes", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void chechpreviousmarked(int i){
        db.collection("personal").document(useremail).collection("attempted").document(timestamp)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String choosed = task.getResult().getString(String.valueOf(i)).toString();
                        if(task.isSuccessful()){
                            check(choosed);
                        }
                    }
                });

    }
    public void check(String chosed) {
        if (a.getText().toString().equals(chosed)) {
            setback();
            optiona.setChecked(true);
        } else if (b.getText().toString().equals(chosed)) {
            setback();
            optionb.setChecked(true);

        } else if (c.getText().toString().equals(chosed)) {
            setback();
            optionc.setChecked(true);
        } else if (d.getText().toString().equals(chosed)) {
            setback();
            optiond.setChecked(true);
        }
    }
    //QUIZ TIMER
    private void startTimer(String duration) {
        try {
            // Parse duration from minutes to milliseconds
            int durationMinutes = Integer.parseInt(duration);
            long durationMillis = durationMinutes * 60 * 1000;

            // Start the countdown timer
            countDownTimer = new CountDownTimer(durationMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // Format remaining time as MM:SS
                    long minutes = millisUntilFinished / (60 * 1000);
                    long seconds = (millisUntilFinished / 1000) % 60;
                    String timeLeft = String.format("%02d:%02d", minutes, seconds);
                    timer.setText(timeLeft);
                }

                @Override
                public void onFinish() {
                    // Timer finished
                    Intent intent = new Intent(quiz.this,quizperformcompleted.class);
                    startActivity(intent);
                    finish();
                }
            }.start();
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid duration format: " + duration, e);
            timer.setText("Error: Invalid duration");
        }
    }

    public void onBackPressed() {
        return;

    }
    protected void onPause() {
        Intent intent= new Intent(quiz.this,quizperformcompleted.class);
        sendfinish(usercode,userpin,"");
        startActivity(intent);
        finish();
        super.onPause();
    }

    @Override
    protected void onStop() {
        Intent intent= new Intent(quiz.this,quizperformcompleted.class);
        sendfinish(usercode,userpin,"");
        startActivity(intent);
        finish();
        super.onStop();
        // Handle actions when the activity is stopped
    }

}