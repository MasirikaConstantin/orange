# orange

les méthodes utilisées pour parvenir à la decouverte ds appareils à proximité

Pour créer une application Android simple qui recherche et affiche les périphériques Bluetooth disponibles

#Étapes pour créer l'application Android de recherche Bluetooth#

#### 1. Création du Projet

Ouvrez Android Studio et créez un nouveau projet avec les paramètres suivants :

- Nom de l'application : TEST_ORANGE_SUMMER_CHALLENGE
- Langage : dans cet exemple, nous utilisons Java
-  SDK  utilisé: 31 (Android 12.0 ) 

 2. Ajout des Permissions

Ajoutez les permissions nécessaires dans le fichier `AndroidManifest.xml` pour le Bluetooth et la localisation :

```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

#### 3. Création du Layout

Modifiez le fichier `activity_main.xml` pour inclure une `ListView` et un bouton :

```<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="245dp"
        android:layout_height="241dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_bias="0.206"
        app:srcCompat="@drawable/orange" />

    <ProgressBar
        android:id="@+id/progress"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="236dp"
        android:layout_height="22dp"
        android:layout_marginTop="80dp"
        app:layout_constraintBottom_toTopOf="@+id/textView3"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintHorizontal_bias="0.474"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/imageView"
        app:layout_constraintVertical_bias="0.079" />

    <TextView
        android:id="@+id/textView3"
        android:layout_width="196dp"
        android:layout_height="49dp"
        android:layout_marginBottom="120dp"
        android:fontFamily="@font/mapolicegrand"
        android:text="Veillez Patientez ..."
        android:textSize="20dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent" />

    <TextView
        android:id="@+id/textViewProgress"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="TextView"
        android:fontFamily="@font/mapolicegrand"
        android:textSize="30dp"
        app:layout_constraintBottom_toTopOf="@+id/textView3"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/progress" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### 4. Implémentation de MainActivity.java

Voici le code pour `MainActivity.java` qui gère la découverte Bluetooth et met à jour la `ListView` :

```java
package com.example.test_orange_summer_challenge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ProgressBar Pro;
    private TextView textViewProgress;
    private int progressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Pro = findViewById(R.id.progress);
        textViewProgress = findViewById(R.id.textViewProgress);

        // Temps total pour l'attente
        final int totalTime = 4000; // en millisecondes

        // Définir la visibilité de la ProgressBar
        Pro.setVisibility(View.VISIBLE);

        // Démarrer un thread pour mettre à jour la ProgressBar
        new Thread(new Runnable() {
            public void run() {
                long startTime = System.currentTimeMillis();

                while (progressStatus < 100) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    progressStatus = (int) (100.0 * elapsedTime / totalTime);

                    // Mettre à jour la barre de progression sur l'UI thread
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Pro.setProgress(progressStatus);
                            textViewProgress.setText(progressStatus + "%"); // Mettre à jour le TextView avec le pourcentage
                        }
                    });

                    try {
                        // Attendre pour la mise à jour suivante
                        Thread.sleep(100); // Ajustez le délai selon vos besoins
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Une fois terminé, démarrer l'activité suivante
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent i = new Intent(getApplicationContext(), Buetooth.class);
                        startActivity(i);
                        finish();
                    }
                });
            }
        }).start();

    }
}


```


#### 5. Implémentation de Buetooh.java

Voici le code pour `Buetooh.java` qui gère la découverte Bluetooth et met à jour la `ListView` :

```java
package com.example.test_orange_summer_challenge;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class Buetooth extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_BLUETOOTH_SCAN_PERMISSION = 3;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesListAdapter;
    private ArrayList<String> devicesList;

    private ListView listView;
    private Button discoverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buetooth);

        listView = findViewById(R.id.listView);
        discoverButton = findViewById(R.id.discoverButton);

        devicesList = new ArrayList<>();
        devicesListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        listView.setAdapter(devicesListAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            checkLocationPermission();
        }

        // Register BroadcastReceiver for Bluetooth discovery
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        // Check and request BLUETOOTH_SCAN permission if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_SCAN_PERMISSION);
            } else {
                startBluetoothDiscovery();
            }
        } else {
            startBluetoothDiscovery();
        }

        // Setup onClick listener for discoverButton
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermission();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startBluetoothDiscovery();
        }
    }

    private void startBluetoothDiscovery() {
        // Vérifie si la permission BLUETOOTH_SCAN est accordée
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Demande la permission si elle n'est pas encore accordée
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    REQUEST_BLUETOOTH_SCAN_PERMISSION);
            return;
        }

        // Si la découverte est déjà en cours, l'annule
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Démarre la découverte Bluetooth
        bluetoothAdapter.startDiscovery();
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(Buetooth.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                String deviceInfo = deviceName + "\n" + deviceAddress;

                if (!devicesList.contains(deviceInfo)) {
                    devicesList.add(deviceInfo);
                    devicesListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Location permission is required to discover Bluetooth devices", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == REQUEST_BLUETOOTH_SCAN_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Bluetooth scan permission is required to discover Bluetooth devices", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}

```

### Une petite Explications du Code

- **Permissions** : Nous demandons la permission `ACCESS_FINE_LOCATION` pour pouvoir rechercher des appareils Bluetooth à proximité.
- **UI** : La `ListView` (`listView`) est utilisée pour afficher la liste des périphériques Bluetooth découverts. Le bouton (`discoverButton`) permet de démarrer la découverte Bluetooth.
- **BluetoothAdapter** : `BluetoothAdapter.getDefaultAdapter()` est utilisé pour obtenir l'instance du BluetoothAdapter.
- **BroadcastReceiver** : `receiver` est enregistré pour détecter les périphériques Bluetooth découverts.
- **Gestion des Permissions** : `checkLocationPermission()` est utilisé pour vérifier et demander la permission `ACCESS_FINE_LOCATION` nécessaire pour la découverte Bluetooth.
- **Démarrage de la Découverte Bluetooth** : `startBluetoothDiscovery()` est appelé lorsque l'utilisateur clique sur le bouton `discoverButton` ou lorsque la permission `ACCESS_FINE_LOCATION` est accordée.
