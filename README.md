# orange

les méthodes utilisées pour parvenir à la decouverte ds appareils à proximité

Pour créer une application Android simple qui recherche et affiche les périphériques Bluetooth disponibles

#Étapes pour créer l'application Android de recherche Bluetooth#

#### 1. Création du Projet

Ouvrez Android Studio et créez un nouveau projet avec les paramètres suivants :

- Nom de l'application** : BluetoothScanner
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

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ListView
        android:id="@+id/listView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp"
        android:divider="@android:color/darker_gray"
        android:dividerHeight="1dp"/>

    <Button
        android:id="@+id/discoverButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Start Bluetooth Discovery"
        android:layout_alignParentBottom="true"
        android:padding="16dp"/>
</RelativeLayout>
```

#### 4. Implémentation de MainActivity.java

Voici le code pour `MainActivity.java` qui gère la découverte Bluetooth et met à jour la `ListView` :

```java
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

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesListAdapter;
    private ArrayList<String> devicesList;

    private ListView listView;
    private Button discoverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothDiscovery();
            } else {
                Toast.makeText(this, "Location permission is required to discover Bluetooth devices", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
```

### Explications du Code

- **Permissions** : Nous demandons la permission `ACCESS_FINE_LOCATION` pour pouvoir rechercher des appareils Bluetooth à proximité.
- **UI** : La `ListView` (`listView`) est utilisée pour afficher la liste des périphériques Bluetooth découverts. Le bouton (`discoverButton`) permet de démarrer la découverte Bluetooth.
- **BluetoothAdapter** : `BluetoothAdapter.getDefaultAdapter()` est utilisé pour obtenir l'instance du BluetoothAdapter.
- **BroadcastReceiver** : `receiver` est enregistré pour détecter les périphériques Bluetooth découverts.
- **Gestion des Permissions** : `checkLocationPermission()` est utilisé pour vérifier et demander la permission `ACCESS_FINE_LOCATION` nécessaire pour la découverte Bluetooth.
- **Démarrage de la Découverte Bluetooth** : `startBluetoothDiscovery()` est appelé lorsque l'utilisateur clique sur le bouton `discoverButton` ou lorsque la permission `ACCESS_FINE_LOCATION` est accordée.

### Conclusion

En suivant ces étapes, vous devriez être en mesure de créer une application Android simple qui recherche et affiche les périphériques Bluetooth disponibles dans une liste. Assurez-vous de tester votre application sur un périphérique Android réel, car certaines fonctionnalités Bluetooth peuvent ne pas être disponibles sur les émulateurs. Assurez-vous également de gérer correctement les permissions et de vérifier les erreurs ou avertissements dans la console Logcat d'Android Studio pour résoudre tout problème potentiel.
