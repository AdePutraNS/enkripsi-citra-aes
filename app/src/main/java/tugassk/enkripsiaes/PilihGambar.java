package tugassk.enkripsiaes;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PilihGambar extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 1;
    EditText inputText, inputPassword;
    TextView outputText, tampilUri;
    String ImageDecode;
    Button encBtn, decBtn, pilihGambar;
    String outputString;
    String AES = "AES";
    ImageView tampilGambar;
    String encodedImage = null;
    Intent intent;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set layout
        setContentView(R.layout.pilih_gambar);

        inputPassword = (EditText) findViewById(R.id.inputPassword);
        //deklarasi untuk ourput text (tampilan output text dibatasi tingginya setinggi 200px agar tidak terlalu panjang)
        outputText = (TextView) findViewById(R.id.outputText);
        tampilUri = (TextView) findViewById(R.id.uri);
        //deklarasi tombol
        encBtn = (Button) findViewById(R.id.encBtn);
        decBtn = (Button) findViewById(R.id.decBtn);
        pilihGambar = (Button) findViewById(R.id.pilihGambar);

        //deklarasi imageview
        tampilGambar = (ImageView) findViewById(R.id.tampilGambar);

        if(encodedImage == null){
            outputText.setText("Belum Ada Hasil Enkripsi atau Gambar yang Dipilih");
        } else {
            outputText.setText(encodedImage);
        }

        //////////////////////////////////////////////////////////////////////////////////////
        //                                                                                  //
        //      setOnClickListener digunakan untuk menghandle aksi jika tombol di klik      //
        //                                                                                  //
        //////////////////////////////////////////////////////////////////////////////////////

        encBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V){
                try {
                    outputString = encrypt(encodedImage, inputPassword.getText().toString());
                    //ambil data string base64 dari gambar yang udah dipilih dan di enkripsi dengan key/password yang dikasi
                    outputText.setText("Hasil Enkripsi : " + outputString);
                    //tampilkan data hasil enkripsi
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        decBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try {
                    outputString = decrypt(outputString, inputPassword.getText().toString());
                    outputText.setText("Hasil Dekripsi : " + outputString);
                    //tampilkan data hasil dekripsi
                } catch (Exception e) {
                    Toast.makeText(PilihGambar.this, "Kunci Enkripsi Salah", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });

        pilihGambar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                // hanya rampilkan gambar
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // tampilkan aplikasi pemilih gambar
                startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
                //hasil dari startActivityForResult akan ditangkap oleh onActivityResult
            }
        });
        

    }

    //fungsi untuk menangkap startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //siapkan gambar buat ditampilin di imageview

                ImageView imageView = (ImageView) findViewById(R.id.tampilGambar);
                imageView.setImageBitmap(bitmap);
                encodedImage = null;
                tampilUri.setText("URI Gambar : " + uri.toString());
                gambar_ke_base64(uri);
                outputText.setText("BASE64 Gambar : " + encodedImage);
                //konversi gambar(bitmap) ke Base64 buat di enkripsi. parsing parameter bentuk URI dari gambarnya yg dipilih
                Toast.makeText(PilihGambar.this, "Gambar Berhasil Dipilih, gambar siap di enkripsi", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void gambar_ke_base64(Uri imagePath){

        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(imagePath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArrayImage = baos.toByteArray();
            encodedImage = null;
            encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            //konversi gambar ke base64 dan simpan kedalam variabel
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //untuk dekripsi parameter berupa string yang ada di outputString sebagai data, dan password yang kita berikan
    private String decrypt(String outputString, String password) throws Exception{
        SecretKeySpec key = generateKey(password);
        //buat kunci berdasarkan password yang kita berikan
        Cipher c = Cipher.getInstance(AES);
        //deklarasi jenis Chiper untuk AES
        c.init(Cipher.DECRYPT_MODE, key);
        //inisialisasi mode dekripsi dengan kunci yang sudah dibuat
        byte[] decodedValue = Base64.decode(outputString, Base64.DEFAULT);
        //mengubah data (dekode) string Base64 menjadi byte untuk di dekripsi
        byte[] decValue = c.doFinal(decodedValue);
        //proses dekripsi hasil dekode data base64
        String decryptedValue = new String(decValue);
        //mengubah data hasil dekripsi yang dalam bentuk byte kedalam string
        return decryptedValue;
        //nilai dekripsi dikembalikan. nilai dekripsi sama dengan nilai konversi gambar ke Base64 pada pertama kali sebelum di enkripsi
    }

    //untuk enkripsi memerlukan data yang berupa variabel encodedImage (deklarasi ada di bawah class, proses enkode pada fungsi gambar_ke_base64, dan pemanggilan pada onClick tombol encBtn) serta password yang kita berikan
    private String encrypt(String data, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        //membuat kunci berdasarkan password yang kita berukan
        Cipher c = Cipher.getInstance(AES);
        //deklarasi jenis Cipher untuk AES
        c.init(Cipher.ENCRYPT_MODE, key);
        //inisialisasi mode enkripsi dengan kunci yang dibuat
        byte[] encVal = c.doFinal(data.getBytes());
        //mengubah string dari data Base64 encodedImage menjadi byte dengan fungsi getBytes() lalu mengenkripsinya dengan cipher yang telah dibuat
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        //mengubah hasil enkripsi dalam bentuk byte kedalam bentuk base64
        return encryptedValue;
        //mengembalikan hasil enkripsi
    }

    //pembuatan kunci dengan password yang sudah ada.
    private SecretKeySpec generateKey(String password) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        //membuat suatu digest dengan algoritma SHA256
        byte[] bytes = password.getBytes("UTF-8");
        //mengubah password menjadi byte
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        //membuat key dalam bentuk byte dengan digest yang telah di deklarasikan
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        //menspesifikasikan kunci untuk enkripsi AES
        return secretKeySpec;
        //mengembalikan nilai kunci
    }
}
