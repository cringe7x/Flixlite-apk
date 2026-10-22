package com.animefix.lite;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(16,17,26), panel = Color.rgb(27,29,43), purple = Color.rgb(164,132,255);
    private final ArrayList<Movie> movies = new ArrayList<>();
    private LinearLayout list;
    private EditText search, urlInput, manualUrlInput;
    private TextView status;
    private String language = "Hindi";
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    static class Movie {
        String title, language, year, link;
        Movie(String t, String l, String y, String u) { title=t; language=l; year=y; link=u; }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        buildUi();
        addSample("Sample: Hindi Movie Catalogue", "Hindi", "2025");
        addSample("Sample: River of Stars", "Japanese", "2025");
        addSample("Sample: Dhaka Memories", "Bangla", "2024");
        addSample("Sample: Moonlight Academy", "Korean", "2025");
        render();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16),dp(20),dp(16),dp(24));
        scroll.addView(root);
        TextView brand = text("AnimeFix Lite", 27, Color.WHITE, true);
        root.addView(brand);
        TextView subtitle = text("Lightweight movie & anime catalogue", 13, Color.LTGRAY, false);
        root.addView(subtitle);
        search = input("Search title or language…");
        LinearLayout.LayoutParams sp = params(-1,-2); sp.topMargin=dp(18); root.addView(search,sp);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int before,int count) { render(); }
            public void afterTextChanged(Editable e) {}
        });
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL);
        String[] langs={"Hindi","All","Bangla","English","Japanese","Mandarin","Korean","Other"};
        for(String l:langs) {
            Button b = new Button(this); b.setText(l); b.setTextSize(12); b.setAllCaps(false);
            b.setTextColor(Color.WHITE); b.setPadding(dp(8),0,dp(8),0);
            b.setBackground(round(language.equals(l)?purple:panel,22));
            if(language.equals(l)) b.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams cp=params(-2,dp(42)); cp.rightMargin=dp(6); chips.addView(b,cp);
            b.setOnClickListener(v->{language=l; for(int i=0;i<chips.getChildCount();i++){Button x=(Button)chips.getChildAt(i);boolean on=x.getText().toString().equals(language);x.setBackground(round(on?purple:panel,22));x.setTextColor(on?Color.BLACK:Color.WHITE);} render();});
        }
        hsv.addView(chips); LinearLayout.LayoutParams hp=params(-1,-2);hp.topMargin=dp(12);root.addView(hsv,hp);
        TextView heading=text("Discover",19,Color.WHITE,true); LinearLayout.LayoutParams hdp=params(-1,-2);hdp.topMargin=dp(18);root.addView(heading,hdp);
        TextView note=text("Hindi is selected first. Sample entries are placeholders. Add a public HTTPS JSON catalogue you are authorized to use.",12,Color.LTGRAY,false);root.addView(note);
        status=text("Ready. Load a catalogue to show its titles.",12,purple,false);LinearLayout.LayoutParams stp=params(-1,-2);stp.topMargin=dp(8);root.addView(status,stp);
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);LinearLayout.LayoutParams lp=params(-1,-2);lp.topMargin=dp(8);root.addView(list,lp);
        TextView add=text("Add a catalogue",19,Color.WHITE,true);LinearLayout.LayoutParams ap=params(-1,-2);ap.topMargin=dp(20);root.addView(add,ap);
        urlInput=input("https://example.com/catalog.json");LinearLayout.LayoutParams up=params(-1,-2);up.topMargin=dp(8);root.addView(urlInput,up);
        Button load=new Button(this);load.setText("Load catalogue");load.setAllCaps(false);load.setTextColor(Color.BLACK);load.setBackground(round(purple,12));LinearLayout.LayoutParams bp=params(-1,dp(48));bp.topMargin=dp(8);root.addView(load,bp);load.setOnClickListener(v->loadCatalogue());
        TextView manualTitle=text("Play a video URL",19,Color.WHITE,true); LinearLayout.LayoutParams mtp=params(-1,-2);mtp.topMargin=dp(22);root.addView(manualTitle,mtp);
        manualUrlInput=input("https://example.com/video.mp4"); LinearLayout.LayoutParams mup=params(-1,-2);mup.topMargin=dp(8);root.addView(manualUrlInput,mup);
        Button playUrl=new Button(this);playUrl.setText("Play URL in app");playUrl.setAllCaps(false);playUrl.setTextColor(Color.BLACK);playUrl.setBackground(round(purple,12));LinearLayout.LayoutParams pup=params(-1,dp(48));pup.topMargin=dp(8);root.addView(playUrl,pup);playUrl.setOnClickListener(v->playManualUrl());
        TextView format=text("JSON format: an array of objects, or an object containing ‘movies’ or ‘results’. Fields: title/name, language/lang, year, url/link/stream_url. Only authorized HTTPS sources are supported; this app does not bypass logins or DRM.",12,Color.LTGRAY,false);LinearLayout.LayoutParams fp=params(-1,-2);fp.topMargin=dp(12);root.addView(format,fp);
        setContentView(scroll);
    }

    private void addSample(String t,String l,String y){movies.add(new Movie(t,l,y,""));}
    private EditText input(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY);e.setHint(hint);e.setTextSize(14);e.setPadding(dp(12),dp(10),dp(12),dp(10));e.setBackground(round(panel,12));return e;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout.LayoutParams params(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}

    private void render(){
        if(list==null)return;
        list.removeAllViews();String q=search==null?"":search.getText().toString().toLowerCase(Locale.ROOT);int count=0;
        for(Movie m:movies){
            if(!language.equals("All")&&!m.language.equalsIgnoreCase(language))continue;
            if(!(m.title+" "+m.language+" "+m.year).toLowerCase(Locale.ROOT).contains(q))continue;
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(14),dp(13),dp(14),dp(13));card.setBackground(round(panel,14));LinearLayout.LayoutParams cparams=params(-1,-2);cparams.bottomMargin=dp(9);list.addView(card,cparams);
            TextView title=text(m.title,16,Color.WHITE,true);card.addView(title);
            TextView meta=text(m.language+(m.year.isEmpty()?"":" · "+m.year),12,Color.LTGRAY,false);LinearLayout.LayoutParams mp=params(-1,-2);mp.topMargin=dp(5);card.addView(meta,mp);
            if(m.link!=null&&!m.link.isEmpty()){
                Button open=new Button(this);open.setText("Play in app");open.setAllCaps(false);open.setTextColor(Color.BLACK);open.setBackground(round(purple,10));LinearLayout.LayoutParams op=params(-1,dp(44));op.topMargin=dp(8);card.addView(open,op);
                open.setOnClickListener(v->openPlayer(m.link,m.title));
            }else{TextView sample=text("Catalogue preview",11,Color.GRAY,false);LinearLayout.LayoutParams sm=params(-1,-2);sm.topMargin=dp(5);card.addView(sample,sm);}
            count++;
        }
        if(count==0){TextView empty=text("No titles found. Try another search or language.",13,Color.LTGRAY,false);empty.setGravity(Gravity.CENTER);empty.setPadding(0,dp(20),0,dp(20));list.addView(empty);}
    }

    private void playManualUrl(){ String raw=manualUrlInput.getText().toString().trim(); if(!raw.toLowerCase(Locale.ROOT).startsWith("https://")){status.setText("Enter a valid HTTPS video URL.");return;} openPlayer(raw,"Online video"); }
    private void openPlayer(String url,String title){ try{ Intent i=new Intent(this,PlayerActivity.class); i.putExtra("url",url); i.putExtra("title",title); startActivity(i);}catch(Exception e){Toast.makeText(this,"Cannot open video",Toast.LENGTH_SHORT).show();} }

    private void loadCatalogue(){
        String raw=urlInput.getText().toString().trim();
        if(!raw.toLowerCase(Locale.ROOT).startsWith("https://")){status.setText("Please enter a valid HTTPS URL.");return;}
        status.setText("Loading catalogue…");
        worker.execute(()->{
            HttpURLConnection conn=null;
            try{
                URL url=new URL(raw);conn=(HttpURLConnection)url.openConnection();conn.setConnectTimeout(10000);conn.setReadTimeout(12000);conn.setRequestProperty("Accept","application/json");conn.setRequestProperty("User-Agent","AnimeFixLite/1.0");
                int code=conn.getResponseCode();if(code<200||code>=300)throw new Exception("HTTP "+code);
                BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));StringBuilder body=new StringBuilder();String line;
                while((line=reader.readLine())!=null&&body.length()<2000000)body.append(line);reader.close();
                Object parsed=new JSONTokener(body.toString()).nextValue();JSONArray arr;
                if(parsed instanceof JSONArray)arr=(JSONArray)parsed;
                else if(parsed instanceof JSONObject&&((JSONObject)parsed).opt("movies") instanceof JSONArray)arr=((JSONObject)parsed).getJSONArray("movies");
                else if(parsed instanceof JSONObject&&((JSONObject)parsed).opt("results") instanceof JSONArray)arr=((JSONObject)parsed).getJSONArray("results");
                else throw new Exception("JSON needs an array, 'movies', or 'results'.");
                ArrayList<Movie> loaded=new ArrayList<>();
                for(int i=0;i<arr.length()&&i<200;i++){JSONObject o=arr.optJSONObject(i);if(o==null)continue;String title=o.optString("title",o.optString("name","Untitled"));String lang=o.optString("language",o.optString("lang","Other"));String year=o.optString("year","");String link=o.optString("url",o.optString("stream_url",o.optString("link","")));loaded.add(new Movie(title,lang,year,link));}
                runOnUiThread(()->{movies.clear();movies.addAll(loaded);language="All";status.setText("Loaded "+loaded.size()+" catalogue entries.");render();((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(urlInput.getWindowToken(),0);});
            }catch(Exception e){String msg=e.getMessage()==null?"Could not load catalogue":e.getMessage();runOnUiThread(()->status.setText("Catalogue error: "+msg));}
            finally{if(conn!=null)conn.disconnect();}
        });
    }
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}