package com.jewelzqiu.sjtubbs.newpost;

import com.jewelzqiu.sjtubbs.R;
import com.jewelzqiu.sjtubbs.support.Utils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by jewelzqiu on 7/7/14.
 */
public class NewPostActivity extends Activity {

    public static final String FLAG_IS_REPLY = "is_reply";

    public static final String REPLY_URL = "url";

    public static final String REPLY_TO = "reply_to_user";

    public static final String BOARD_NAME = "board";

    private static final int REQUEST_CODE_CAMERA = 6841368;

    private static final int REQUEST_CODE_GALLERY = 6841369;

    private boolean isReply;

    private String replyToUser;

    private String boardName;

    private ArrayList<NameValuePair> postValues = new ArrayList<NameValuePair>();

    private EditText titleText;

    private EditText contentText;

    private Dialog mDialog;

    private String photoPath;

    private Uri[] photoUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        titleText = (EditText) findViewById(R.id.edittext_title);
        contentText = (EditText) findViewById(R.id.edittext_content);

        postValues.clear();
        Intent intent = getIntent();
        isReply = intent.getBooleanExtra(FLAG_IS_REPLY, false);
        replyToUser = intent.getStringExtra(REPLY_TO);
        boardName = intent.getStringExtra(BOARD_NAME);
        new GetPostValuesTask().execute(intent.getStringExtra(REPLY_URL));

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(android.R.color.holo_blue_dark));
        tintManager.setTintAlpha(0.69f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_cancel:
                finish();
                break;
            case R.id.action_upload:
                selectPictures();
                break;
            case R.id.action_send:
                new PostTask().execute();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CAMERA:
                System.out.println(photoPath);
                break;

            case REQUEST_CODE_GALLERY:
                if (data == null) {
                    return;
                }
                if (data.getData() != null) {
                    photoUris = new Uri[1];
                    photoUris[0] = data.getData();
//                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
//                    Cursor cursor = getContentResolver()
//                            .query(photoUris[0], filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//                    String path = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
//                    cursor.close();
//                    System.out.println(path);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            Uri[] uris = new Uri[clipData.getItemCount()];
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                uris[i] = clipData.getItemAt(i).getUri();
                            }
                            photoUris = uris;
                        }
                    }
                }
                new UploadTask().execute(photoUris);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void selectPictures() {
        if (mDialog == null) {
            ListView listView = new ListView(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    getResources().getStringArray(R.array.pic_select_options));
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new OnSelectPhotoListener());
            mDialog = new AlertDialog.Builder(this).setView(listView).create();
        }
        mDialog.show();
    }

    private class OnSelectPhotoListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent;
            switch (position) {
                case 0: // take a photo
                    photoPath = Utils.getPhotoPath();
                    File photoFile = new File(photoPath);
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(intent, REQUEST_CODE_CAMERA);
                    break;

                case 1: // select from gallery
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("image/*");
                        startActivityForResult(intent, REQUEST_CODE_GALLERY);
                    } else {
                        intent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQUEST_CODE_GALLERY);
                    }
                    break;
            }
            mDialog.dismiss();
        }
    }

    private class GetPostValuesTask extends AsyncTask<String, Void, Void> {

        private String title;

        private String content;

        @Override
        protected Void doInBackground(String... params) {
            postValues.add(new BasicNameValuePair("signature", "1"));
            postValues.add(new BasicNameValuePair("autocr", "on"));
            postValues.add(new BasicNameValuePair("up", ""));
            postValues.add(new BasicNameValuePair("MAX_FILE_SIZE", "1048577"));
            postValues.add(new BasicNameValuePair("level", "0"));
            postValues.add(new BasicNameValuePair("live", "180"));
            postValues.add(new BasicNameValuePair("exp", "0"));
            postValues.add(new BasicNameValuePair("board", boardName));

            if (isReply) {
                postValues.add(new BasicNameValuePair(REPLY_TO, replyToUser));

                try {
                    Document document = Jsoup.connect(params[0]).cookies(Utils.cookies).get();
                    Elements elements = document.getElementsByTag("input");
                    for (Element element : elements) {
                        if (element.attr("name").equals("file")) {
                            postValues.add(new BasicNameValuePair("file", element.attr("value")));
                        } else if (element.attr("name").equals("reidstr")) {
                            postValues
                                    .add(new BasicNameValuePair("reidstr", element.attr("value")));
                        } else if (element.attr("name").equals("title")) {
                            title = element.attr("value");
                        }
                    }

                    Element textArea = document.select("#text").first();
                    content = '\n' + textArea.text();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                postValues.add(new BasicNameValuePair("file", ""));
                postValues.add(new BasicNameValuePair("reidstr", ""));
                postValues.add(new BasicNameValuePair(REPLY_TO, ""));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            titleText.setText(title);
            contentText.setText(content);
        }
    }

    private class PostTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            postValues.add(new BasicNameValuePair("title", titleText.getText().toString()));
            postValues.add(new BasicNameValuePair("text", contentText.getText().toString()));

            try {
                HttpPost httpPost = new HttpPost(Utils.BBS_BASE_URL + "/bbssnd");
                DefaultHttpClient client = new DefaultHttpClient();
                httpPost.addHeader("Cookie", Utils.getCookies());
                httpPost.addHeader("Connection", "keep-alive");
                httpPost.setEntity(new UrlEncodedFormEntity(postValues, "GB2312"));
                HttpResponse httpResponse = client.execute(httpPost);
                return EntityUtils.toString(httpResponse.getEntity());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s == null || s.contains("ERROR")) {
                Toast.makeText(getApplicationContext(), getString(R.string.post_failed),
                        Toast.LENGTH_SHORT).show();
                if (Utils.isAutoLoginEnabled(getApplicationContext())) {
                    Utils.autoLogin(getApplicationContext());
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.post_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UploadTask extends AsyncTask<Uri, Integer, String> {

        @Override
        protected String doInBackground(Uri... uris) {
            for (Uri uri : uris) {
                try {
                    HttpPost httpPost = new HttpPost(Utils.BBS_BASE_URL + "/bbsdoupload");

                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    httpPost.addHeader("Cookie", Utils.getCookies());
                    httpPost.addHeader("Connection", "keep-alive");

                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                    builder.addTextBody("board", boardName);
                    builder.addTextBody("level", "0");
                    builder.addTextBody("live", "180");
                    builder.addTextBody("exp", "");
                    builder.addTextBody("MAX_FILE_SIZE", "1048577");

                    File file = Utils.saveTempFile(NewPostActivity.this, uri);
                    builder.addBinaryBody("up", file, ContentType.APPLICATION_FORM_URLENCODED,
                            file.getName());

                    httpPost.setEntity(builder.build());
                    HttpResponse response = httpClient.execute(httpPost);

                    String responseHtml = EntityUtils.toString(response.getEntity());

                    Document doc = Jsoup.parse(responseHtml);
                    String url = doc.select("p > font").text();
                    return url;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.upload_failed),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getApplicationContext(), getString(R.string.upload_success),
                    Toast.LENGTH_SHORT).show();
            int start = Math.max(contentText.getSelectionStart(), 0);
            int end = Math.max(contentText.getSelectionEnd(), 0);
            contentText.getText().replace(Math.min(start, end), Math.max(start, end),
                    result, 0, result.length());
        }
    }
}
