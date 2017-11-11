package lm.pkp.com.landmap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.res.disp.AreaItemAdaptor;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.sync.LocalDataRefresher;

public class AreaDashboardActivity extends AppCompatActivity {

    private AreaDBHelper adb = null;
    private ArrayList<AreaElement> allAreas = null;
    private AreaItemAdaptor areaDisplayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_dashboard);
        getSupportActionBar().hide();

        Toolbar topTB = (Toolbar) findViewById(R.id.toolbar_top);
        topTB.inflateMenu(R.menu.area_db_top_menu);

        adb = new AreaDBHelper(getApplicationContext());
        allAreas = adb.getAllAreas();

        ListView areaListView = (ListView) findViewById(R.id.area_display_list);
        areaDisplayAdapter = new AreaItemAdaptor(this, R.layout.area_element_row, allAreas);
        areaListView.setAdapter(areaDisplayAdapter);

        ActionMenuItemView createAreaView = (ActionMenuItemView) findViewById(R.id.action_area_create);
        createAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AreaElement ae = adb.insertAreaLocally();
                AreaContext.getInstance().setAreaElement(ae, getApplicationContext());
                adb.insertAreaToServer(ae);

                Intent intent = new Intent(AreaDashboardActivity.this, PositionMarkerActivity.class);
                startActivity(intent);
            }
        });

        ActionMenuItemView refreshAreaView = (ActionMenuItemView) findViewById(R.id.action_area_refresh);
        refreshAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                new LocalDataRefresher(getApplicationContext(), new DataReloadCallback()).refreshLocalData();
            }
        });

        areaListView.setDescendantFocusability(ListView.FOCUS_BLOCK_DESCENDANTS);
        areaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                AreaElement ae = (AreaElement) adapter.getItemAtPosition(position);
                AreaContext.getInstance().setAreaElement(ae, getApplicationContext());
                Intent intent = new Intent(AreaDashboardActivity.this, PositionMarkerActivity.class);
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

    private class DataReloadCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            areaDisplayAdapter.clear();
            areaDisplayAdapter.addAll(adb.getAllAreas());
            areaDisplayAdapter.notifyDataSetChanged();
            findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);
        }
    }
}
