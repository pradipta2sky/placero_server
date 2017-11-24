package lm.pkp.com.landmap;

import android.R.drawable;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaDashboardDisplayMetaStore;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardOwnedFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardPublicFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardSharedFragment;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.FragmentIdentificationHandler;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.permission.PermissionConstants;
import lm.pkp.com.landmap.permission.PermissionElement;
import lm.pkp.com.landmap.permission.PermissionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.util.ColorProvider;

public class AreaDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        this.setContentView(R.layout.activity_area_dashboard);
        // Setup Toolbar
        Toolbar toolbar = (Toolbar) this.findViewById(id.areas_display_toolbar);
        this.setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        ViewPager viewPager = (ViewPager) this.findViewById(id.areas_display_tab_pager);
        // Assign created adapter to viewPager
        viewPager.setAdapter(new DisplayAreasPagerAdapter(this.getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(1);

        TabLayout tabLayout = (TabLayout) this.findViewById(id.areas_display_tab_layout);
        // This method setup all required method for TabLayout with Viewpager
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        View.OnClickListener createListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(id.splash_panel).setVisibility(View.VISIBLE);
                AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new DataInsertServerCallback());
                AreaElement areaElement = adh.insertAreaLocally();

                PermissionElement pe = new PermissionElement();
                pe.setUserId(UserContext.getInstance().getUserElement().getEmail());
                pe.setAreaId(areaElement.getUniqueId());
                pe.setFunctionCode(PermissionConstants.FULL_CONTROL);

                PermissionsDBHelper pdh = new PermissionsDBHelper(getApplicationContext());
                pdh.insertPermissionLocally(pe);
                areaElement.getUserPermissions().put(PermissionConstants.FULL_CONTROL, pe);

                // Resetting the context for new Area
                AreaContext.INSTANCE.setAreaElement(areaElement, getApplicationContext());
                adh.insertAreaToServer(areaElement);
            }
        };
        ImageView createAreaView = (ImageView) this.findViewById(id.action_area_create);
        createAreaView.setOnClickListener(createListener);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        AreaElement areaElement = AreaContext.INSTANCE.getAreaElement();
        if(areaElement != null){
            TabLayout tabLayout = (TabLayout) this.findViewById(id.areas_display_tab_layout);
            Integer position = AreaDashboardDisplayMetaStore.INSTANCE.getTabPositionByAreaType(areaElement.getType());
            tabLayout.getTabAt(position).select();
        }
    }


    public static class DisplayAreasPagerAdapter extends FragmentPagerAdapter {

        private Map<Integer, Fragment> store = new HashMap<>();

        public DisplayAreasPagerAdapter(FragmentManager fm) {
            super(fm);
            store.put(0, new AreaDashboardOwnedFragment());
            store.put(1, new AreaDashboardSharedFragment());
            store.put(2, new AreaDashboardPublicFragment());
        }

        @Override
        // For each tab different fragment is returned
        public Fragment getItem(int position) {
            return store.get(position);
        }


        @Override
        public int getCount() {
            return store.size();

        }

        @Override
        public CharSequence getPageTitle(int position) {
            FragmentIdentificationHandler identification = (FragmentIdentificationHandler) store.get(position);
            return identification.getFragmentTitle();
        }
    }

    @Override
    public void onBackPressed() {
        new Builder(this).setIcon(drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure?")
                .setPositiveButton("yes", new OnClickListener() {
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

    private class DataInsertServerCallback implements AsyncTaskCallback {
        @Override
        public void taskCompleted(Object result) {
            Intent intent = new Intent(getApplicationContext(), CreateAreaFolderStructureActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
