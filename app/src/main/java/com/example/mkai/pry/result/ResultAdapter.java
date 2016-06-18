package com.example.mkai.pry.result;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mkai.pry.R;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class ResultAdapter extends BaseAdapter {
    private Context context;
    private List<PersonDescriptor> items;
    private int widthDisplay;

    public ResultAdapter(List<PersonDescriptor> items, Context context) {
        super();
        this.items = items;
        this.context = context;
        this.widthDisplay = 30;
    }

    /**
     * Создание адаптера
     * @param items список данных
     * @param context контект
     * @param widthDisplay ширина фотографии
     */
    public ResultAdapter(List<PersonDescriptor> items, Context context, int widthDisplay)
    {
        super();
        this.items = items;
        this.context = context;
        this.widthDisplay = widthDisplay;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public PersonDescriptor getItem(int position) {
        return this.items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = mInflater.inflate(R.layout.result_item, null);
        }
        final ImageView ivPhoto = (ImageView) v.findViewById(R.id.photo);
        final TextView tvInfo = (TextView) v.findViewById(R.id.person_info);
//        final TextView tvBirthday = (TextView) v.findViewById(R.id.birthday);
//        final TextView tvCity = (TextView) v.findViewById(R.id.city);
//        final TextView tvWork = (TextView) v.findViewById(R.id.work);
//        final TextView tvPhone = (TextView) v.findViewById(R.id.phone);

        final String photo = items.get(position).getPhoto();
        final String name = items.get(position).getName();
        final String birthday = items.get(position).getBirthday();
        final String work = items.get(position).getWork();
        final String phone = items.get(position).getPhone();
        String city_str = new String();
        if ((items.get(position).getCountry() != "") && (items.get(position).getCity() != ""))
            city_str = items.get(position).getCountry() + ", " + items.get(position).getCity();

        final String city = city_str;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final URL url = new URL(photo);
                    final Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(url.openStream()),300,300,false);
                    ivPhoto.post(new Runnable() {
                        @Override
                        public void run() {
                            ivPhoto.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        String info_str = new String();
        if (name != null && name != "")
            info_str += name + "\n";
        if (birthday != null && birthday != "")
            info_str += birthday + "\n";
        if (city != null && city != "")
            info_str += city + "\n";
        if (phone != null && phone != "")
            info_str += phone + "\n";
        if (work != null && work != "")
            info_str += work + "\n";
        tvInfo.setText(info_str);

//        if (name == null || name == "")
//            tvName.setVisibility(View.INVISIBLE);
//        else
//            tvName.setText(name);
//        if (birthday == null || birthday == "")
//            tvBirthday.setVisibility(View.INVISIBLE);
//        else
//            tvBirthday.setText(birthday);
//        tvCity.setText(city);
//        tvPhone.setText(phone);
//        tvWork.setText(work);
        return v;
    }
}
