package jp.ac.meijou.s231205036.android.schedulestrengthcalendar;


import android.content.Context;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.Preferences.Key;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.Optional;

import io.reactivex.rxjava3.core.Single;


public class PrefDataStore {
    private static PrefDataStore instance;
    private final RxDataStore<Preferences> dataStore;

    public PrefDataStore(RxDataStore<Preferences> dataStore) {
        this.dataStore = dataStore;
    }

    public static PrefDataStore getInstance(Context context){
        if (instance == null){
            var dataStore = new RxPreferenceDataStoreBuilder(
                    context.getApplicationContext(),"settings").build();
            instance = new PrefDataStore(dataStore);
        }
        return instance;
    }

    public void setString(String key, String value){
        dataStore.updateDataAsync(prefsIN -> {
                    var mutablePreferences = prefsIN.toMutablePreferences();
                    var prefKey = PreferencesKeys.stringKey(key);

                    mutablePreferences.set(prefKey,value);
                    return Single.just(mutablePreferences);
                })
                .subscribe();
    }

    public Optional<String> getString(String key) {
        return dataStore.data()
                .map(prefs -> {
                    var prefKey = PreferencesKeys.stringKey(key);
                    return Optional.ofNullable(prefs.get(prefKey));
                })
                .blockingFirst();
    }

    public Optional<Integer> getInteger(String key) {
        return dataStore.data()
                .map(prefs -> {
                    Key<Integer> prefKey = PreferencesKeys.intKey(key);
                    return Optional.ofNullable(prefs.get(prefKey));
                })
                .blockingFirst();
    }

    public void setInteger(String key, int value) {
        dataStore.updateDataAsync(prefsIN -> {
                    var mutablePreferences = prefsIN.toMutablePreferences();
                    var prefKey = PreferencesKeys.intKey(key);

                    mutablePreferences.set(prefKey, value);
                    return Single.just(mutablePreferences);
                })
                .subscribe();
    }

}

