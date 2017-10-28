package lm.pkp.com.landmap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.AreaItemDisplayAdaptor;

public class AreaDashboardActivity extends AppCompatActivity {

    private AreaDBHelper adb = null;
    private ArrayList<AreaElement> allAreas = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_dashboard);
        getSupportActionBar().hide();

        Toolbar topTB = (Toolbar) findViewById(R.id.toolbar_top);
        topTB.inflateMenu(R.menu.area_db_top_menu);

        adb = new AreaDBHelper(getApplicationContext());
        allAreas = adb.getAllAreas();

        ListView areaListView = (ListView) findViewById(R.id.area_display_list);
        if (allAreas.size() > 0) {
            AreaItemDisplayAdaptor areaDisplayAdapter = new AreaItemDisplayAdaptor(this, R.layout.area_element_row, allAreas);
            areaListView.setAdapter(areaDisplayAdapter);
        } else {
            areaListView.setVisibility(View.INVISIBLE);
            // TODO Display a text or image for no areaas
        }

        ActionMenuItemView createAreaView = (ActionMenuItemView) findViewById(R.id.action_area_create);
        createAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AreaElement ae = adb.insertAreaLocally();
                adb.insertAreaToServer(ae);

                Intent intent = new Intent(AreaDashboardActivity.this, PositionMarkerActivity.class);
                intent.putExtra("area_name", ae.getName());
                startActivity(intent);
            }
        });

        areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        areaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                AreaElement ae = (AreaElement) adapter.getItemAtPosition(position);
                Intent intent = new Intent(AreaDashboardActivity.this, PositionMarkerActivity.class);
                intent.putExtra("area_name", ae.getName());
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("no", null).show();
    }
}
