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

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardOwnedFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardPublicFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardSharedFragment;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
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

        this.setContentView(layout.activity_area_dashboard);
        // Setup Toolbar
        Toolbar toolbar = (Toolbar) this.findViewById(id.areas_display_toolbar);
        this.setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        ViewPager viewPager = (ViewPager) this.findViewById(id.areas_display_tab_pager);
        // Assign created adapter to viewPager
        viewPager.setAdapter(new AreaDashboardActivity.DisplayAreasPagerAdapter(this.getSupportFragmentManager()));

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

    public static class DisplayAreasPagerAdapter extends FragmentPagerAdapter {
        // As we are implementing two tabs
        private static final int NUM_ITEMS = 3;

        public DisplayAreasPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        // For each tab different fragment is returned
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AreaDashboardOwnedFragment();
                case 1:
                    return new AreaDashboardSharedFragment();
                case 2:
                    return new AreaDashboardPublicFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return DisplayAreasPagerAdapter.NUM_ITEMS;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Owned";
                case 1:
                    return "Shared";
                case 2:
                    return "Public";
                default:
                    return null;
            }
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
            finish();
            Intent intent = new Intent(getApplicationContext(), CreateAreaFolderStructureActivity.class);
            startActivity(intent);
        }
    }

}
