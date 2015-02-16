package org.hackersir.checkin;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private TextView textview;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFilters;
    private String[][] nfcTechLists;
    private NfcAdapter nfc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 把TextView 叫到這裡 顯示卡號用
        textview = (TextView) findViewById(R.id.main_textview);

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFilters = new IntentFilter[]{tech};

        nfcTechLists = new String[][]{new String[]{NfcA.class.getName()}};

        // 叫控制NFC的Adapter出來用
        // Application Context 可以讓整個程式都能用
        nfc = NfcAdapter.getDefaultAdapter(getApplicationContext());

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Activity 離開使用者畫面時 暫時關閉NFC偵測
        nfc.disableForegroundDispatch(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Activity 還原(啟動)時 開啟NFC偵測
        nfc.enableForegroundDispatch(this, pendingIntent, intentFilters, nfcTechLists);
    }

    /* 掃描到NFC卡片時要做的事情
         * 原本是onNewIntent 但沒有其他的intent要處理 所以直接做處理NFC卡片的動作 */
    @Override
    public void onNewIntent(Intent intent) {
        // 從 intent 抓NFC卡片號碼出來顯示在textview
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        textview.setText(byteArrayToString(tag.getId()));
    }

    /* 把2進位的卡片ID轉成16進位String */
    private String byteArrayToString(byte[] id) {
        // 每個 byte 轉成hex時要占用兩個char
        char[] hexArray = new char[id.length * 2];

        // 處理每個byte時先處理Most Significant Bit 數來4位
        // 再處理 Least Significant Bit 4位數
        // 用 mask( 0x0F ) 遮掉不要的資料後 再轉為 hex char
        for (int i = 0; i < id.length; i++) {
            hexArray[i * 2] = Character.forDigit((id[i] >>> 4) & 0x0F, 16);
            hexArray[i * 2 + 1] = Character.forDigit(id[i] & 0x0F, 16);
        }

        return new String(hexArray);
    }
}
