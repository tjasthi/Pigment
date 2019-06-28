package com.example.pigment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

import static com.example.pigment.DiagnosticActivity.COMPLETED_TEST;

// Adapter for the recycler view in MainActivity.
public class QuestionAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private ArrayList<Question> mQuestions;
    private ArrayList<EditText> mInputs;
    private int VIEW_TYPE_HEADER = 2;
    private int VIEW_TYPE_FOOTER = 1;
    private int VIEW_TYPE_CELL = 0;

    public QuestionAdapter(Context context, ArrayList<Question> Questions) {
        mContext = context;
        mQuestions = Questions;
        mInputs = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // here, we specify what kind of view each cell should have. In our case, all of them will have a view
        // made from comment_cell_layout
        if (viewType == VIEW_TYPE_CELL) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.question_cell_layout, parent, false);
            return new QuestionViewHolder(view, mInputs);
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.results_cell_layout, parent, false);
            return new ResultsViewHolder(view, mContext, mInputs, mQuestions);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.intro_cell_layout, parent, false);
            return new IntroViewHolder(view);
        }
    }


    // - get element from your dataset at this position
    // - replace the contents of the view with that element
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            ((IntroViewHolder) holder).bind();
        } else if (position <= mQuestions.size()) {
            Question q = mQuestions.get(position - 1);
            ((QuestionViewHolder) holder).bind(q, position, mQuestions.size());
        } else {
            ((ResultsViewHolder) holder).bind();
        }
    }

    // Return the size of your Questions (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mQuestions.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mQuestions.size() + 1) {
            return VIEW_TYPE_FOOTER;
        } else if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_CELL;
        }
    }
}

class QuestionViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public RelativeLayout mQuestionBubbleLayout;
    public ImageView mImageView;
    public EditText mInput;

    public QuestionViewHolder(View itemView, ArrayList<EditText> inputs) {
        super(itemView);
        itemView.setClickable(false);
        mQuestionBubbleLayout = itemView.findViewById(R.id.question_cell_layout);
        mInput = mQuestionBubbleLayout.findViewById(R.id.input);
        inputs.add(mInput);
        mImageView = mQuestionBubbleLayout.findViewById(R.id.question_image_view);
        mImageView.setFocusable(false);
    }

    void bind(final Question Question, int position, int size) {
        if (position == size) {
            mInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        mImageView.setImageURI(Question.imageUri);
    }
}

class ResultsViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public RelativeLayout mResultsBubbleLayout;
    public Button mButton;
    public Context mContext;
    public ArrayList<EditText> mInputs;
    private ArrayList<Question> mQuestions;

    public ResultsViewHolder(View itemView, Context context, ArrayList<EditText> inputs, ArrayList<Question> questions) {
        super(itemView);
        itemView.setClickable(false);
        itemView.setFocusable(false);
        mContext = context;
        mInputs = inputs;
        mResultsBubbleLayout = itemView.findViewById(R.id.results_cell_layout);
        mButton = mResultsBubbleLayout.findViewById(R.id.button);
        mQuestions = questions;
    }

    void bind() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean answered = true;
                ArrayList<String> answers = new ArrayList<>();
                ArrayList<String> n = new ArrayList<>();
                ArrayList<String> p = new ArrayList<>();
                ArrayList<String> d = new ArrayList<>();
                for (int i = 0; i < mInputs.size(); i++) {
                    EditText input = mInputs.get(i);
                    String answer = input.getText().toString();
                    if (TextUtils.isEmpty(answer)) {
                        answered = false;
                        input.requestFocus();
                        Toast.makeText(mContext,"You must answer all questions to see results",Toast.LENGTH_SHORT).show();
                    } else {
                        answers.add(answer);
                        n.add(mQuestions.get(i).normal);
                        p.add(mQuestions.get(i).protanopia);
                        d.add(mQuestions.get(i).deuteranopia);
                    }
                }
                if (answered) {
                    // User has seen OnboardingFragment, so mark our SharedPreferences
                    // flag as completed so that we don't show our OnboardingFragment
                    // the next time the user launches the app.
                    SharedPreferences.Editor sharedPreferencesEditor =
                            PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    sharedPreferencesEditor.putBoolean(
                            DiagnosticActivity.COMPLETED_TEST, true);
                    sharedPreferencesEditor.apply();
                    // start main activity
                    Intent main = new Intent(mContext, MainActivity.class);
                    main.putExtra("answers", answers);
                    main.putExtra("normal", n);
                    main.putExtra("protanopia", p);
                    main.putExtra("deuteranopia", d);
                    mContext.startActivity(main);
                }
            }
        });
    }
}

class IntroViewHolder extends RecyclerView.ViewHolder {

    // each data item is just a string in this case
    public RelativeLayout mTextBubbleLayout;
    public TextView mText;

    public IntroViewHolder(View itemView) {
        super(itemView);
        itemView.setClickable(false);
        mTextBubbleLayout = itemView.findViewById(R.id.description);
        mText = mTextBubbleLayout.findViewById(R.id.intro);
    }

    void bind() {
        mText.setText(R.string.test_description);
    }
}
