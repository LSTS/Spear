package pt.lsts.spear;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

import static pt.lsts.spear.MainActivity.imc;

public class SysInteractions extends AppCompatActivity {

    TextView area;
    TextView line;
    TextView sms;
    TextView compass;
    String selected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_inter);

        android.app.ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        area = findViewById(R.id.area);
        line = findViewById(R.id.line);
        sms = findViewById(R.id.sms);
        compass = findViewById(R.id.compass);
        getIntentSelected();

    }

    public void getIntentSelected() {
        Intent intent = getIntent();
        selected = Objects.requireNonNull(intent.getExtras()).getString("selected");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void area(View view) {
        Intent i = new Intent(SysInteractions.this, Area_.class);
        i.putExtra("selected", imc.selectedVehicle);
        startActivity(i);


    }

    public void line(View view) {
        Intent i = new Intent(SysInteractions.this, Line.class);
        i.putExtra("selected", imc.selectedVehicle);
        startActivity(i);
    }


    public void sms(View view) {
        Intent i = new Intent(SysInteractions.this, StaticListVehicles.class);
        startActivity(i);
    }


    public void compass(View view) {
        Intent i = new Intent(SysInteractions.this, Compass.class);
        i.putExtra("selected", imc.selectedVehicle);
        startActivity(i);
    }
}