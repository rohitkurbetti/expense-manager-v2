package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapters.DataAdapter;
import com.example.myapplication.adapterholders.DataModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DataAdapter dataAdapter;
    private List<DataModel> dataList;
    private FirebaseFirestore db;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recycler_view);

        recyclerView = findViewById(R.id.recyclerView);
        SearchView searchView = findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pd = new ProgressDialog(this);
        dataList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
//        fetchDataFromFirestore();
        getAllDocumentsFromCollection("invoices");


        dataAdapter = new DataAdapter(dataList);
        dataAdapter = new DataAdapter(dataList, new DataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String docId) {
                // Handle item click, fetch and display details
                fetchDocumentDetails(docId);
            }

            @Override
            public boolean onLongClick(String date, String id) {
                showAlertDialog(date, id);
//                toggleSelection(id);
                return false;
            }
        });

        recyclerView.setAdapter(dataAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                dataAdapter.filter(newText);
                return false;
            }
        });


//        dataList.add(new DataModel("title1","subtitle1","2024-05-17",null));
//        dataList.add(new DataModel("title2","subtitle2","2024-05-17",null));
//        dataList.add(new DataModel("title3","subtitle3","2024-05-17",null));

//        dataAdapter.notifyDataSetChanged();


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }

    private void toggleSelection(String documentId) {
        for (DataModel dataModel : dataList) {
            if (dataModel.getId().equals(documentId)) {
                dataModel.setSelected(!dataModel.isSelected());
                dataAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void showAlertDialog(String date, String id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecyclerViewActivity.this);

        View customTitleView = getLayoutInflater().inflate(R.layout.dialog_title, null);

        // Set the title text and image
        ImageView dialogTitleIcon = customTitleView.findViewById(R.id.dialogTitleIcon);
        TextView dialogTitleText = customTitleView.findViewById(R.id.dialogTitleText);

        dialogTitleIcon.setImageResource(R.drawable.ic_delete); // Replace with your image resource
        dialogTitleText.setText("Do you want to delete invoice "+id+" ?");

        builder.setCustomTitle(customTitleView);

//        builder.setTitle("Do you want to delete invoice "+id+" ?");
        builder.setMessage("Id in the cloud is: \n\n'"+(date+"_"+id)+"'");
        builder.setCancelable(false);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteFromFirebaseById(date, id);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void deleteFromFirebaseById(String date, String id) {
        String cloudId = date+"_"+id;
        db.collection("invoices")
                .document(cloudId).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted  "+cloudId, Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error while deletion "+e.getMessage(), Toast.LENGTH_LONG).show());
        for(DataModel data : dataList){
            if(data.getId().equals(id)){
                dataList.remove(data);
                break;
            }
        }
        dataAdapter.notifyDataSetChanged();
    }

    private void fetchDocumentDetails(String id) {
        pd.show();
        db.collection("invoices").document(id).get()
                .addOnSuccessListener(document -> {
                    pd.dismiss();
                    if (document.exists()) {
                        Long idVal = document.getLong("invoice_id");
                        Long total =document.getLong("total");
                        String createdDate = document.getString("created_date");
                        String createdDateTime = document.getString("created_date_time");
                        String jsonList = document.getString("item_list_json");

                        DataModel dataModel = new DataModel(String.valueOf(idVal),
                                "Invoice. "+idVal,
                                "Total: "+total,
                                createdDateTime,
                                jsonList,
                                createdDate);

                        Intent intent = new Intent(this, DetailActivity.class);
                        intent.putExtra("dataModel", (Serializable) dataModel);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Doc not exists ", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Error getting doc "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void getAllDocumentsFromCollection(String invoices) {
        pd.setMessage("Loading please wait");
        pd.show();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection(invoices);

        Query query = collectionRef.orderBy("invoice_id", Query.Direction.DESCENDING);

        // Fetch all documents in the collection
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        pd.dismiss();
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        // Process the documents

                        for (DocumentSnapshot document : documents) {
//                            Log.d(TAG, document.getId() + " => " + document.getData());
                            Long id = document.getLong("invoice_id");
                            Long total =document.getLong("total");
                            String createdDate = document.getString("created_date");
                            String createdDateTime = document.getString("created_date_time");
                            String jsonList = document.getString("item_list_json");

                            dataList.add(new DataModel(String.valueOf(id),
                                    "Invoice. "+id,
                                    "Total: "+total,
                                    createdDateTime,
                                    null,
                                    createdDate));

                        }
                        new DataAdapter(dataList);
                        dataAdapter.notifyDataSetChanged();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(RecyclerViewActivity.this, "Error getting documents from collection "+invoices, Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void fetchDataFromFirestore() {
        db.collection("data")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                DataModel dataModel = document.toObject(DataModel.class);
                                dataList.add(dataModel);
                            }
                            dataAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Error getting documents", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}