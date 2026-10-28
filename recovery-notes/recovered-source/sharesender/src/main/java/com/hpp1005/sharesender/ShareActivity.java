package com.hpp1005.sharesender;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.FileProvider;
import java.io.File;

public class ShareActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        String name = getIntent().getStringExtra("name");
        if (name == null || name.isEmpty()) name = "smoke-test.pdf";
        File fixture = new File(getFilesDir(), name);
        Uri uri = FileProvider.getUriForFile(this, "com.hpp1005.sharesender.fileprovider", fixture);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setClassName("com.hpp1005.printer", "com.mohamedelnaggar.hpp1005.MainActivity");
        startActivity(share);
        finish();
    }
}
