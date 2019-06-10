package com.liu.mytest1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import uk.co.senab.photoview.PhotoView;

/**
 * @创建者 ly
 * @创建时间 2019/5/31
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class ImageFragment extends Fragment {

    private PhotoView hvImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_image, null);
        hvImage = inflate.findViewById(R.id.hvImage);
        Glide.with(getContext()).load(getArguments().getString("url")).apply(new RequestOptions().override(Target.SIZE_ORIGINAL)).into((hvImage));
        return inflate;
    }

    public static ImageFragment newInstance(String url) {
        ImageFragment f = new ImageFragment();

        Bundle args = new Bundle();
        args.putString("url", url);
        f.setArguments(args);
        return f;
    }

}
