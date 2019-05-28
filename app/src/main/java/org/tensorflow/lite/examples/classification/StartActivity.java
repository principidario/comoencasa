package org.tensorflow.lite.examples.classification;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    ListView list;
    //AdapterClass adapter;
    SearchView editsearch;
    String[] searchQueries;
    ArrayList<SearchQuery> arraylist = new ArrayList<SearchQuery>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        searchQueries = new String[] {
                "TextView", "ListView", "SearchView", "RatingBar", "ToolBar", "Button", "EditText",
                "ToggleButton", "ImageView", "SlidingDrawer", "Android"};

        list = findViewById(R.id.list_view);
        for (String searchQuery:searchQueries) {
            SearchQuery searchQuery1 = new SearchQuery(searchQuery);
            // Binds all strings into an array
            arraylist.add(searchQuery1);
        }

        //adapter = new AdapterClass(this, arraylist);
        //list.setAdapter(adapter);
        editsearch = findViewById(R.id.search_view);
        editsearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                //adapter.filter(text);
                return false;
            }
        });

        /*
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }

}

class SearchQuery {

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private String query;

    public SearchQuery(String query) {
        this.query = query;
    }
}