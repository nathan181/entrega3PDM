package br.com.jackson.mywalkintime;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private static final int GPS_REQ_CODE = 1001;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private boolean isAlreadyPermissionGranted=false;
    private boolean isAlreadyEnabled=false;
    private boolean isOnPercurso=false;
    private boolean isOnWalk=true;


    private Chronometer elapsedTimeChronometer;


    private Button grantPermissionGpsButton;
    private Button enableGpsButton;
    private Button disableGpsButton;
    private Button startRouteButton;
    private Button stopRouteButton;

    private TextView locationTextView;
    private EditText localSearcheditText;

    private double latitude;
    private double longitude;
    private double distance=0.0;


    private Location localAnt;

    private String pesquisa;

    private void configurarGPS() {

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);


        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                if(isOnPercurso){

                    if(isOnWalk){
                        localAnt = location;
                        isOnWalk = false;
                    }

                        double distanciaAnt = localAnt.distanceTo(location);

                        distance += distanciaAnt;

                        String distKM = String.format(Locale.getDefault(), " %.2f KM ", distance/1000d);

                        locationTextView.setText(distKM);

                        localAnt = location;


                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationTextView = findViewById(R.id.locationTextView);
        localSearcheditText = findViewById(R.id.localSearcheditText);
        elapsedTimeChronometer = findViewById(R.id.elapsedTimeChronometer);
        grantPermissionGpsButton = findViewById(R.id.grantPermissionGpsButton);
        enableGpsButton = findViewById(R.id.enableGpsButton);
        disableGpsButton = findViewById(R.id.disableGpsButton);
        startRouteButton = findViewById(R.id.startRouteButton);
        stopRouteButton = findViewById(R.id.stopRouteButton);



        localSearcheditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                pesquisa = s.toString();

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        configurarGPS();

        grantPermissionGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isAlreadyPermissionGranted){
                    Toast.makeText(getApplicationContext(), getString(R.string.permission_already_granted), Toast.LENGTH_SHORT).show();

                }
                else{
                    grantPermission();

                }
            }
        });

        enableGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAlreadyPermissionGranted){
                    Toast.makeText(getApplicationContext(), getString(R.string.no_gps_no_app), Toast.LENGTH_SHORT).show();
                }else{
                    if(!isAlreadyEnabled){
                        grantPermission();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), getString(R.string.gps_already_activated), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        disableGpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAlreadyEnabled){
                    locationManager.removeUpdates(locationListener);
                    isAlreadyEnabled = false;
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.gps_not_activated), Toast.LENGTH_SHORT).show();
                }

            }
        });

        startRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAlreadyEnabled) {

                    isOnPercurso = true;
                    Toast.makeText(getApplicationContext(), getString(R.string.started_chronometer), Toast.LENGTH_SHORT).show();
                    elapsedTimeChronometer.setBase(SystemClock.elapsedRealtime());
                    elapsedTimeChronometer.start();

                    elapsedTimeChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                        @Override
                        public void onChronometerTick(Chronometer chronometer) {
                            elapsedTimeChronometer.setFormat(" %s");
                        }
                    });

                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.no_gps_activation), Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isOnPercurso){
                    Toast.makeText(getApplicationContext(), locationTextView.getText() + " - " + elapsedTimeChronometer.getText(), Toast.LENGTH_SHORT).show();
                    elapsedTimeChronometer.stop();
                    elapsedTimeChronometer.setBase(SystemClock.elapsedRealtime());
                    locationTextView.setText(R.string.route_location_done);
                    isOnPercurso = false;
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.no_route_iniciated), Toast.LENGTH_SHORT).show();
                }



            }
        });

        FloatingActionButton fab = findViewById(R.id.searchFloatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(
                        String.format(
                                Locale.getDefault(),
                                "geo:%f, %f?q="+pesquisa,
                                latitude,
                                longitude
                        )
                );
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        uri
                );
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //a permissão já foi dada?
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            isAlreadyPermissionGranted = true;
        }
        else{
            isAlreadyEnabled = false;
        }

    }

    public void grantPermission(){

        //a permissão já foi dada?
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            //somente ativa

            isAlreadyPermissionGranted = true;
            isAlreadyEnabled = true;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, locationListener);
        }
        else{
            //permissão ainda não foi nada, solicita ao usuário
            //quando o usuário responder, o método
            //onRequestPermissionsResult vai ser chamado
            ActivityCompat.requestPermissions(this,
                    new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1001){
            if (grantResults.length > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                //permissão concedida, ativamos o GPS
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED){

                    isAlreadyEnabled = true;
                    isAlreadyPermissionGranted = true;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, locationListener);
                }
            }
            else{
                //usuário negou, não ativamos
                isAlreadyEnabled = false;
                isAlreadyPermissionGranted = false;
                Toast.makeText(this, getString(R.string.no_gps_no_app),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}

