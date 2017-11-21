package lm.pkp.com.landmap;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.area.res.doc.AreaDocumentChooserFragment;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;

public class AreaDocumentChooserActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GenericActivityExceptionHandler(this);

        this.setContentView(layout.activity_area_doc_chooser);

        Toolbar toolbar = (Toolbar) this.findViewById(id.fc_toolbar);
        toolbar.setTitle("Choose Document PDF");

        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(id.fragment_container, new AreaDocumentChooserFragment(),
                AreaDocumentChooserFragment.class.getSimpleName());
        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
