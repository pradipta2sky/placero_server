package lm.pkp.com.landmap;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.util.AreaActivityUtil;
import lm.pkp.com.landmap.util.ColorConstants;

public class AreaEditActivity extends AppCompatActivity{

    private AreaDBHelper adb = null;
    private AreaElement ae = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_edit);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setBackgroundDrawable(new ColorDrawable(ColorConstants.getToolBarColorForShare()));
        ab.show();

        adb = new AreaDBHelper(getApplicationContext());
        ae = AreaContext.getInstance().getAreaElement();

        View includedView = findViewById(R.id.selected_area_include);
        AreaActivityUtil.INSTANCE.populateAreaElement(includedView);

        final TextView nameText = (TextView)findViewById(R.id.area_name_edit);
        nameText.setText(ae.getName());

        final TextView descText = (TextView)findViewById(R.id.area_desc_edit);
        descText.setText(ae.getDescription());

        final TextView addressText = (TextView)findViewById(R.id.area_address_edit);
        addressText.setText(ae.getAddress());

        Button saveButton = (Button)findViewById(R.id.area_edit_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

                ae.setName(nameText.getText().toString());
                ae.setDescription(descText.getText().toString());
                ae.setAddress(addressText.getText().toString());

                adb.updateAreaNonGeo(ae);
                findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);

                Intent positionMarkerIntent = new Intent(AreaEditActivity.this, AreaDetailsActivity.class);
                startActivity(positionMarkerIntent);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
