package com.example.bigthingthatidk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

public class LetterAdapter extends BaseAdapter {

    private String[] letters;
    private LayoutInflater letterinflater;

    public LetterAdapter(Context context) {
        letters = new String[26];
        for (int a=0;a<letters.length;a++)
        {
            letters[a] = "" + (char)(a+'A');
        }
        letterinflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return letters.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Button letterBtn;
        if (view == null) {
            letterBtn = (Button)letterinflater.inflate(R.layout.letter, viewGroup, false); //tao cac nut bam tai vi tri bang chu cai
        } else {
            letterBtn = (Button) view;
        }
        letterBtn.setText(letters[i]);
        return letterBtn;
    }
}

