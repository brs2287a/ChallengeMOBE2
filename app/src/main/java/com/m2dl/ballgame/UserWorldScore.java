package com.m2dl.ballgame;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserWorldScore#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserWorldScore extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private ArrayList<String> data;
    private int viewType;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String guid = Accueil.sharedPreferences.getString("GUID", Accueil.guidNotRetrieve());
    private int userPos;

    public UserWorldScore() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserWorldScore.
     */
    // TODO: Rename and change types and number of parameters
    public static UserWorldScore newInstance(int viewType) {
        UserWorldScore fragment = new UserWorldScore();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, viewType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            viewType = getArguments().getInt(ARG_PARAM1);
        }

    }


    private void init(View v) {
        userPos = -1;
        data = new ArrayList<>();
        ProgressBar pb = v.findViewById(R.id.progressBar_cyclic);
        pb.setVisibility(View.VISIBLE);
        switch (viewType) {
            case Score.LOCALSCORES:
                List<Integer> scores = Accueil.loadArray(getContext().getApplicationContext());
                for (int i = 0; i < scores.size(); ++i) {
                    data.add("TOP " + (i + 1) + ": " + scores.get(i));
                }
                inflate(v, data);
                break;
            case Score.TOP100:
                db.collection("score").orderBy("score", Query.Direction.DESCENDING).limit(100)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        data.add(getStringFromDoc(document));
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                    inflate(v, data);
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                break;
            case Score.MYTOP:
                //highscore du user
                db.collection("score").document(guid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //les 50 scores au dessus
                                db.collection("score").orderBy("score", Query.Direction.DESCENDING).limitToLast(50).endAt(document).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        data.add(getStringFromDoc(document));
                                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                                    }
                                                    userPos = data.size() - 1;
                                                    //les 50 scores en dessous
                                                    db.collection("score").orderBy("score", Query.Direction.DESCENDING).limit(50).startAfter(document)
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            data.add(getStringFromDoc(document));
                                                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                                                        }
                                                                        inflate(v, data);
                                                                    } else {
                                                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
                break;
        }

    }

    private String getStringFromDoc(QueryDocumentSnapshot document) {
        Map<String, Object> doc = document.getData();
        String str = "";
        str += doc.get("user") + " : " + doc.get("score");
        return str;
    }

    private void inflate(View v, ArrayList<String> data) {
        TableLayout table = (TableLayout) v.findViewById(R.id.table_user_score);
        if (data != null) {
            for (int i = 0; i < data.size(); ++i) {
                final TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.item_score, null);
                TextView tvTitre = (TextView) tr.findViewById(R.id.tvContenu);
                tvTitre.setText(data.get(i));
                if (i == userPos) {
                    tr.setBackgroundColor(Color.parseColor("#FAE37B"));
                }
                if ((viewType == Score.TOP100 || viewType == Score.LOCALSCORES) && i == 0) {
                    tr.setBackgroundColor(Color.parseColor("#FAE37B"));
                }
                if ((viewType == Score.TOP100 || viewType == Score.LOCALSCORES) && i == 1) {
                    tr.setBackgroundColor(Color.parseColor("#BDBDBD"));
                }
                if ((viewType == Score.TOP100 || viewType == Score.LOCALSCORES) && i == 2) {
                    tr.setBackgroundColor(Color.parseColor("#FA9541"));
                }
                table.addView(tr);
            }
        }
        ProgressBar pb = v.findViewById(R.id.progressBar_cyclic);
        pb.setVisibility(View.GONE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_world_score, container, false);
        if (data == null || data.isEmpty()) {
            init(v);
        }
        return v;

    }
}