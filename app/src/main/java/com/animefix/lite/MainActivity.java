package com.animefix.lite;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
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
    private final int bg = Color.rgb(8, 10, 22);
    private final int panel = Color.rgb(19, 23, 43);
    private final int panel2 = Color.rgb(27, 32, 58);
    private final int purple = Color.rgb(166, 93, 255);
    private final int cyan = Color.rgb(51, 229, 255);
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
        addSample("Beyond the Sky", "Hindi", "2025");
        addSample("River of Stars", "Japanese", "2025");
        addSample("Dhaka Memories", "Bangla", "2024");
        addSample("Moonlight Academy", "Korean", "2025");
        addSample("Neon Samurai", "Japanese", "2024");
        addSample("Midnight Promise", "English", "2025");
        render();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(16),dp(18),dp(28));
        scroll.addView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout logo = new LinearLayout(this);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(roundGradient(new int[]{purple, cyan}, 15));
        TextView x = text("X", 23, Color.WHITE, true);
        x.setGravity(Gravity.CENTER);
        logo.addView(x, params(dp(46),dp(46)));
        top.addView(logo, params(dp(46),dp(46)));
        LinearLayout brandStack = new LinearLayout(this);
        brandStack.setOrientation(LinearLayout.VERTICAL);
        brandStack.setPadding(dp(11),0,0,0);
        brandStack.addView(text("ZEROX TV", 22, Color.WHITE, true));
        brandStack.addView(text("ANIME  •  MOVIES  •  SERIES", 9, cyan, true));
        top.addView(brandStack, new LinearLayout.LayoutParams(0,-2,1));
        TextView live = text("●  LIVE", 10, cyan, true);
        live.setPadding(dp(10),dp(7),dp(10),dp(7));
        live.setBackground(round(panel2,18));
        top.addView(live);
        root.addView(top);

        TextView greeting = text("Your next obsession.", 27, Color.WHITE, true);
        LinearLayout.LayoutParams gp=params(-1,-2); gp.topMargin=dp(24); root.addView(greeting,gp);
        TextView sub = text("A universe of stories, all in one place.", 13, Color.rgb(163,174,204), false);
        LinearLayout.LayoutParams subp=params(-1,-2); subp.topMargin=dp(4); root.addView(sub,subp);

        search = input("⌕   Search anime, movies, language...");
        LinearLayout.LayoutParams sp=params(-1,dp(52)); sp.topMargin=dp(18); root.addView(search,sp);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) {}
            public void onTextChanged(CharSequence s,int st,int before,int count) { render(); }
            public void afterTextChanged(Editable e) {}
        });

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(18),dp(18),dp(18),dp(18));
        hero.setBackground(roundGradient(new int[]{Color.rgb(45,24,83),Color.rgb(13,67,91),Color.rgb(20,25,51)},20));
        LinearLayout.LayoutParams herop=params(-1,dp(176)); herop.topMargin=dp(18); root.addView(hero,herop);
        TextView tag=text("✦  EDITOR'S PICK",10,cyan,true); hero.addView(tag);
        TextView feature=text("BEYOND\nTHE SKY",25,Color.WHITE,true);
        LinearLayout.LayoutParams fp=params(-1,-2); fp.topMargin=dp(8); hero.addView(feature,fp);
        TextView fmeta=text("Anime film  •  2025  •  Adventure",11,Color.rgb(216,223,245),false);
        LinearLayout.LayoutParams fmp=params(-1,-2); fmp.topMargin=dp(3); hero.addView(fmeta,fmp);
        Button explore = new Button(this); explore.setText("Explore catalogue  →"); explore.setAllCaps(false); explore.setTextSize(12);
        explore.setTextColor(Color.WHITE); explore.setBackground(roundGradient(new int[]{purple,Color.rgb(104,90,255)},12));
        LinearLayout.LayoutParams ebp=params(dp(178),dp(40)); ebp.topMargin=dp(10); hero.addView(explore,ebp);
        explore.setOnClickListener(v->{ if(list!=null) list.requestFocus(); scroll.smoothScrollTo(0,list.getTop()); });

        TextView categories=text("BROWSE BY LANGUAGE",10,Color.rgb(150,164,201),true);
        LinearLayout.LayoutParams catp=params(-1,-2);catp.topMargin=dp(22);root.addView(categories,catp);
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout chips = new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL);
        String[] langs={"Hindi","All","Bangla","English","Japanese","Mandarin","Korean","Other"};
        for(String l:langs) {
            TextView chip = text(l,12,language.equals(l)?Color.BLACK:Color.rgb(217,224,246),true);
            chip.setGravity(Gravity.CENTER); chip.setPadding(dp(15),0,dp(15),0);
            chip.setBackground(round(language.equals(l)?cyan:panel2,22));
            LinearLayout.LayoutParams cp=params(-2,dp(38)); cp.rightMargin=dp(7); chips.addView(chip,cp);
            chip.setOnClickListener(v->{language=l; for(int i=0;i<chips.getChildCount();i++){TextView c=(TextView)chips.getChildAt(i);boolean on=c.getText().toString().equals(language);c.setBackground(round(on?cyan:panel2,22));c.setTextColor(on?Color.BLACK:Color.rgb(217,224,246));} render();});
        }
        hsv.addView(chips); LinearLayout.LayoutParams hp=params(-1,-2);hp.topMargin=dp(10);root.addView(hsv,hp);

        LinearLayout section = new LinearLayout(this);
        section.setGravity(Gravity.CENTER_VERTICAL);
        section.setOrientation(LinearLayout.HORIZONTAL);
        TextView heading=text("Discover",22,Color.WHITE,true);
        section.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        TextView badge=text("POSTER GRID",9,cyan,true);badge.setPadding(dp(9),dp(6),dp(9),dp(6));badge.setBackground(round(panel2,12));section.addView(badge);
        LinearLayout.LayoutParams sh=params(-1,-2);sh.topMargin=dp(24);root.addView(section,sh);
        status=text("Ready • Load your authorized catalogue to see titles.",11,Color.rgb(158,171,205),false);
        LinearLayout.LayoutParams stp=params(-1,-2);stp.topMargin=dp(6);root.addView(status,stp);
        list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp=params(-1,-2);lp.topMargin=dp(14);root.addView(list,lp);
        
        LinearLayout continueFolder = folder(root, "◷", "Continue Watching", "Your last played series or movie is saved automatically", 18);
        LinearLayout continueBody = folderBody(continueFolder);
        TextView continueInfo = text("No recent video yet. Play a title or direct video and it will appear here.",12,Color.rgb(158,171,205),false);
        continueBody.addView(continueInfo);
        Button resumeLast = new Button(this);resumeLast.setText("▶  Resume last video");resumeLast.setAllCaps(false);resumeLast.setTextColor(Color.BLACK);resumeLast.setBackground(round(cyan,12));
        LinearLayout.LayoutParams rlp=params(-1,dp(46));rlp.topMargin=dp(10);continueBody.addView(resumeLast,rlp);
        resumeLast.setOnClickListener(v->resumeLastVideo());
        refreshContinueWatching(continueInfo, resumeLast);

        // Separate expandable folders keep the home catalogue clean.
        LinearLayout catalogueFolder = folder(root, "▤", "Catalogue Settings", "Load or refresh your HTTPS JSON catalogue", 18);
        LinearLayout catalogueBody = folderBody(catalogueFolder);
        TextView helper=text("Paste a public HTTPS JSON catalogue you are allowed to use.",12,Color.rgb(158,171,205),false);
        catalogueBody.addView(helper);
        urlInput=input("https://example.com/catalog.json");
        LinearLayout.LayoutParams up=params(-1,dp(50));up.topMargin=dp(10);catalogueBody.addView(urlInput,up);
        Button load=new Button(this);load.setText("Load catalogue");load.setAllCaps(false);load.setTextColor(Color.WHITE);load.setBackground(roundGradient(new int[]{purple,Color.rgb(94,93,255)},12));
        LinearLayout.LayoutParams bp=params(-1,dp(48));bp.topMargin=dp(8);catalogueBody.addView(load,bp);load.setOnClickListener(v->loadCatalogue());

        LinearLayout directFolder = folder(root, "▶", "Direct Video URL", "Play a supported stream link directly", 12);
        LinearLayout directBody = folderBody(directFolder);
        TextView manualHint=text("MP4, HLS or other supported direct stream URL",12,Color.rgb(158,171,205),false);
        directBody.addView(manualHint);
        manualUrlInput=input("https://example.com/video.mp4");
        LinearLayout.LayoutParams mup=params(-1,dp(50));mup.topMargin=dp(10);directBody.addView(manualUrlInput,mup);
        Button playUrl=new Button(this);playUrl.setText("▶   Play in Zerox TV");playUrl.setAllCaps(false);playUrl.setTextColor(Color.BLACK);playUrl.setBackground(round(cyan,12));
        LinearLayout.LayoutParams pup=params(-1,dp(48));pup.topMargin=dp(8);directBody.addView(playUrl,pup);playUrl.setOnClickListener(v->playManualUrl());
        TextView directNote=text("HTTPS links only. The video must be directly accessible and supported by the player.",11,Color.rgb(132,145,177),false);
        LinearLayout.LayoutParams dnp=params(-1,-2);dnp.topMargin=dp(10);directBody.addView(directNote,dnp);

        LinearLayout settingsFolder = folder(root, "⚙", "Settings", "App information and quick actions", 12);
        LinearLayout settingsBody = folderBody(settingsFolder);
        TextView appInfo=text("ZEROX TV  •  Version 1.0\nNeon anime theme  •  Media3 player\nCatalogue and direct playback require public HTTPS sources.",12,Color.rgb(184,194,220),false);
        settingsBody.addView(appInfo);
        Button clearSearch=new Button(this);clearSearch.setText("Clear search");clearSearch.setAllCaps(false);clearSearch.setTextColor(Color.WHITE);clearSearch.setBackground(round(panel2,12));
        LinearLayout.LayoutParams csp=params(-1,dp(44));csp.topMargin=dp(10);settingsBody.addView(clearSearch,csp);
        clearSearch.setOnClickListener(v->{if(search!=null)search.setText("");language="All";render();Toast.makeText(this,"Search cleared",Toast.LENGTH_SHORT).show();});
        TextView format=text("Catalogue JSON: an array or an object containing movies/results. Fields: title/name, language/lang, year, url/link/stream_url.",11,Color.rgb(132,145,177),false);
        LinearLayout.LayoutParams formatp=params(-1,-2);formatp.topMargin=dp(12);settingsBody.addView(format,formatp);
        setContentView(scroll);
    }


    private LinearLayout folder(LinearLayout parent,String icon,String title,String subtitle,int topMargin){
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setBackground(round(panel,16));
        LinearLayout.LayoutParams cp=params(-1,-2);cp.topMargin=dp(topMargin);parent.addView(card,cp);
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.HORIZONTAL);header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14),dp(14),dp(14),dp(14));header.setBackground(round(panel,16));card.addView(header);
        TextView glyph=text(icon,22,cyan,true);glyph.setGravity(Gravity.CENTER);header.addView(glyph,params(dp(36),dp(36)));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.setPadding(dp(10),0,0,0);
        labels.addView(text(title,15,Color.WHITE,true));TextView detail=text(subtitle,10,Color.rgb(158,171,205),false);
        LinearLayout.LayoutParams dp1=params(-1,-2);dp1.topMargin=dp(3);labels.addView(detail,dp1);
        header.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        TextView arrow=text("⌄",22,purple,true);header.addView(arrow);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(14),0,dp(14),dp(15));body.setVisibility(View.GONE);card.addView(body);
        header.setOnClickListener(v->{boolean open=body.getVisibility()!=View.VISIBLE;body.setVisibility(open?View.VISIBLE:View.GONE);arrow.setText(open?"⌃":"⌄");});
        return card;
    }
    private LinearLayout folderBody(LinearLayout folder){
        return (LinearLayout)folder.getChildAt(1);
    }
    private void refreshContinueWatching(TextView info,Button resume){
        android.content.SharedPreferences prefs=getSharedPreferences("zerox_playback",MODE_PRIVATE);
        String title=prefs.getString("last_title","");
        String url=prefs.getString("last_url","");
        long position=prefs.getLong("last_position",0L);
        if(url.isEmpty()){
            info.setText("No recent video yet. Play a title or direct video and it will appear here.");
            resume.setEnabled(false);resume.setAlpha(0.5f);
        }else{
            info.setText("Last watched: "+(title.isEmpty()?"Untitled video":title)+"\\nSaved position: "+formatTime(position));
            resume.setEnabled(true);resume.setAlpha(1f);
        }
    }
    private String formatTime(long ms){
        long total=Math.max(0,ms)/1000;return String.format(Locale.ROOT,"%02d:%02d",total/60,total%60);
    }
    private void resumeLastVideo(){
        android.content.SharedPreferences prefs=getSharedPreferences("zerox_playback",MODE_PRIVATE);
        String url=prefs.getString("last_url","");String title=prefs.getString("last_title","Last video");
        long position=prefs.getLong("last_position",0L);
        if(url.isEmpty()){Toast.makeText(this,"Nothing to resume yet",Toast.LENGTH_SHORT).show();return;}
        try{Intent i=new Intent(this,PlayerActivity.class);i.putExtra("url",url);i.putExtra("title",title);i.putExtra("resume_position",position);startActivity(i);}
        catch(Exception e){Toast.makeText(this,"Cannot resume video",Toast.LENGTH_SHORT).show();}
    }
    private void addSample(String t,String l,String y){movies.add(new Movie(t,l,y,""));}
    private EditText input(String hint){EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.rgb(125,137,169));e.setHint(hint);e.setTextSize(13);e.setPadding(dp(14),dp(10),dp(14),dp(10));e.setBackground(round(panel2,13));e.setSelectAllOnFocus(false);return e;}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);return t;}
    private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private GradientDrawable roundGradient(int[] colors,int radius){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,colors);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout.LayoutParams params(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}

    private void render(){
        if(list==null)return;
        list.removeAllViews();
        String q=search==null?"":search.getText().toString().toLowerCase(Locale.ROOT);
        ArrayList<Movie> visible=new ArrayList<>();
        for(Movie m:movies){
            if(!language.equals("All")&&!m.language.equalsIgnoreCase(language))continue;
            if(!(m.title+" "+m.language+" "+m.year).toLowerCase(Locale.ROOT).contains(q))continue;
            visible.add(m);
        }
        if(visible.isEmpty()){
            TextView empty=text("No titles found. Try another search or language.",13,Color.rgb(184,194,220),false);
            empty.setGravity(Gravity.CENTER);empty.setPadding(0,dp(28),0,dp(28));list.addView(empty);return;
        }
        int columns=2;
        for(int i=0;i<visible.size();i+=columns){
            LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rp=params(-1,-2);rp.bottomMargin=dp(12);list.addView(row,rp);
            for(int j=i;j<Math.min(i+columns,visible.size());j++){
                Movie m=visible.get(j);
                LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(round(panel,15));card.setClipToPadding(false);
                card.setPadding(dp(7),dp(7),dp(7),dp(10));
                LinearLayout.LayoutParams cardp=new LinearLayout.LayoutParams(0,-2,1);cardp.rightMargin=dp(j==i?7:0);cardp.leftMargin=dp(j==i?0:7);row.addView(card,cardp);
                int[] colors=posterColors(m);
                LinearLayout poster=new LinearLayout(this);poster.setOrientation(LinearLayout.VERTICAL);poster.setGravity(Gravity.BOTTOM|Gravity.LEFT);
                poster.setPadding(dp(10),dp(10),dp(10),dp(12));poster.setBackground(roundGradient(colors,11));
                LinearLayout.LayoutParams pp=params(-1,dp(164));card.addView(poster,pp);
                TextView emblem=text(posterEmblem(m),35,Color.WHITE,true);emblem.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams ep=params(-1,0);ep.weight=1;poster.addView(emblem,ep);
                TextView genre=text(m.language.toUpperCase(Locale.ROOT)+"  ✦",9,Color.WHITE,true);poster.addView(genre);
                TextView title=text(m.title,14,Color.WHITE,true);title.setMaxLines(2);title.setPadding(dp(3),dp(10),dp(3),0);card.addView(title);
                TextView meta=text(m.year.isEmpty()?m.language:m.year+"  •  "+m.language,10,Color.rgb(151,165,199),false);
                meta.setPadding(dp(3),dp(4),dp(3),0);card.addView(meta);
                if(m.link!=null&&!m.link.isEmpty()){
                    Button open=new Button(this);open.setText("▶ Play");open.setTextSize(11);open.setAllCaps(false);open.setTextColor(Color.BLACK);open.setBackground(round(cyan,9));
                    LinearLayout.LayoutParams op=params(-1,dp(38));op.topMargin=dp(9);card.addView(open,op);
                    open.setOnClickListener(v->openPlayer(m.link,m.title));
                } else {
                    TextView preview=text("CATALOGUE PREVIEW",8,Color.rgb(123,137,174),true);
                    preview.setPadding(dp(3),dp(9),dp(3),0);card.addView(preview);
                }
            }
            if(visible.size()%2==1&&i+1>=visible.size()){
                View spacer=new View(this);row.addView(spacer,new LinearLayout.LayoutParams(0,1,1));
            }
        }
    }
    private int[] posterColors(Movie m){
        String s=(m.title+" "+m.language).toLowerCase(Locale.ROOT);
        if(s.contains("japan")||s.contains("samurai"))return new int[]{Color.rgb(90,35,133),Color.rgb(17,49,93),Color.rgb(8,16,38)};
        if(s.contains("bangla")||s.contains("dhaka"))return new int[]{Color.rgb(0,105,112),Color.rgb(22,52,91),Color.rgb(8,16,38)};
        if(s.contains("korean")||s.contains("moon"))return new int[]{Color.rgb(117,39,102),Color.rgb(53,44,116),Color.rgb(8,16,38)};
        if(s.contains("english")||s.contains("midnight"))return new int[]{Color.rgb(24,93,126),Color.rgb(42,45,113),Color.rgb(8,16,38)};
        return new int[]{Color.rgb(116,48,177),Color.rgb(36,49,111),Color.rgb(8,16,38)};
    }
    private String posterEmblem(Movie m){
        String s=m.title.trim();
        if(s.isEmpty())return "✦";
        return s.substring(0,1).toUpperCase(Locale.ROOT)+"✦";
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
                URL url=new URL(raw);conn=(HttpURLConnection)url.openConnection();conn.setConnectTimeout(10000);conn.setReadTimeout(12000);conn.setRequestProperty("Accept","application/json");conn.setRequestProperty("User-Agent","ZeroxTV/1.0");
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