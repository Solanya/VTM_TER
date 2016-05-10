package com.vtm.solanya.imgtrt;

import android.app.Application;
import android.content.Context;

import org.acra.*;
import org.acra.annotation.*;

// Le seul intérêt de cette partie consiste à pouvoir récupérer les messages d'erreur lors de l'utilisation de l'application
// par les utilisateurs.

@ReportsCrashes(
        formUri = "https://collector.tracepot.com/dd5666d9"     // Changer cette adresse pour changer la destination des messages d'erreurs
)
public class otTERApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }
}
