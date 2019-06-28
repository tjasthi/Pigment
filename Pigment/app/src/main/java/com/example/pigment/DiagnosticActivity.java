package com.example.pigment;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

public class DiagnosticActivity extends AppCompatActivity {
    //recyclerView
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Question> mQuestions = new ArrayList<Question>();
    public static String COMPLETED_TEST = "completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic);

        //set up recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.question_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new QuestionAdapter(this, mQuestions);
        mRecyclerView.setAdapter(mAdapter);

        makeQuestions();
    }

    private void makeQuestions() {
        //get json string
        InputStream is = getResources().openRawResource(R.raw.questions);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (Exception e) {
            Log.e("JSON ERR", e.getMessage());
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                Log.e("JSON ERR", e.getMessage());
            }

        }
        String jsonString = writer.toString();

        //parse into JSONobjects & create landmarks
        try {
            JSONArray arr = new JSONArray(jsonString);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject jsonobject = arr.getJSONObject(i);


                //get normal answer
                String n = jsonobject.getString("normal");
                //get protanopia answer
                String p = jsonobject.getString("p");
                //get deuteranopia answer
                String d = jsonobject.getString("d");
                //get image
                String imageName = jsonobject.getString("filename");
                // get the imageUri from a drawable resource file
                Uri imageUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/drawable/" + imageName);

                Question q = new Question(imageUri, n, p, d);
                mQuestions.add(q);
                mAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e("JSON ERR", e.getMessage());
        }
    }
}
