package lm.pkp.com.landmap;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.position.PositionElement;
import lm.pkp.com.landmap.position.PositionsDBHelper;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserInfoSearchAsyncTask;
import lm.pkp.com.landmap.util.AreaActivityUtil;

public class AreaShareActivity extends AppCompatActivity implements AsyncTaskCallback{

    private AreaDBHelper adh = null;
    private ArrayAdapter<String> adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_share);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();

        final PositionsDBHelper pdh = new PositionsDBHelper(getApplicationContext());
        AreaActivityUtil.INSTANCE.populateAreaElement(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{});
        final AutoCompleteTextView userIdView = (AutoCompleteTextView) findViewById(R.id.user_search_text);
        userIdView.setAdapter(adapter);

        userIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                UserInfoSearchAsyncTask searcherTask = new UserInfoSearchAsyncTask();
                JSONObject searchParams = new JSONObject();
                try {
                    searchParams.put("ss", s.toString());
                    searchParams.put("sf", "name");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searcherTask.setCompletionCallback(AreaShareActivity.this);
                searcherTask.execute(searchParams);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.area_share_role_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ViewStub stub = (ViewStub) findViewById(R.id.share_details_stub);
                if(stub == null){
                    // View stub is already inflated
                    View inflatedStub = findViewById(R.id.share_details_stub_restricted);
                    if(inflatedStub != null){
                        if(inflatedStub.getVisibility() == View.VISIBLE){
                            inflatedStub.setVisibility(View.GONE);
                        }else {
                            inflatedStub.setVisibility(View.VISIBLE);
                        }
                    }
                }else {
                    stub.setLayoutResource(R.layout.area_share_restricted);
                    stub.inflate();
                }
            }
        });

        Button saveButton = (Button)findViewById(R.id.area_share_save_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will change. There will no longer be an area copy.
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);

                AreaElement copiedArea = AreaContext.getInstance().getAreaElement().copy();
                copiedArea.setCreatedBy(userIdView.getText().toString());
                copiedArea.setUniqueId(UUID.randomUUID().toString());
                adh.insertAreaToServer(copiedArea);

                List<PositionElement> positions = copiedArea.getPositions();
                for (int i = 0; i < positions.size(); i++) {
                    PositionElement pe = positions.get(i);
                    pe.setUniqueId(UUID.randomUUID().toString());
                    pe.setUniqueAreaId(copiedArea.getUniqueId());
                    pdh.insertPositionToServer(pe);
                }
                finish();
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

    @Override
    public void taskCompleted(Object result) {
        try {
            String userArray = result.toString();
            String currUserEmail = UserContext.getInstance().getUserElement().getEmail();
            adapter.clear();

            if(userArray.trim().equalsIgnoreCase("[]")){
                // do nothing.
            }else {
                JSONArray responseArr = new JSONArray(userArray);
                for (int i = 0; i < responseArr.length(); i++) {
                    JSONObject responseObj = (JSONObject) responseArr.get(i);
                    String emailStr = responseObj.getString("email");
                    String displayName = responseObj.getString("display_name");
                    if(!currUserEmail.equalsIgnoreCase(emailStr)){
                        adapter.add(emailStr);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
