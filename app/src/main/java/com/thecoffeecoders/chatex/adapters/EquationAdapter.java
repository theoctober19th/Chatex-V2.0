package com.thecoffeecoders.chatex.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.db.EquationContract;
import com.thecoffeecoders.chatex.interfaces.OnAdapterItemClicked;

import io.github.kexanie.library.MathView;

public class EquationAdapter extends RecyclerView.Adapter<EquationAdapter.EquationViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    private OnAdapterItemClicked mCallback;

    public EquationAdapter(Context context, Cursor cursor, OnAdapterItemClicked listener){
        mContext = context;
        mCursor = cursor;
        this.mCallback = listener;
    }

    @NonNull
    @Override
    public EquationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_single_equation, viewGroup, false);
        return  new EquationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EquationViewHolder viewHolder, int i) {
        if(!mCursor.moveToPosition(i)){
            return;
        }
        String equation = mCursor.getString(
                mCursor.getColumnIndex(EquationContract.EquationEntry.COLUMN_EQUATION)
        );
        viewHolder.bind(equation);

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public class EquationViewHolder extends RecyclerView.ViewHolder{

        MathView mathView;

        public EquationViewHolder(@NonNull View itemView) {
            super(itemView);
            mathView = itemView.findViewById(R.id.suggestion_mathview);
        }
        private void bind(final String equation){
            mathView.setText("\\(" + equation + "\\)");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onAdapterItemClicked(equation);
                }
            });
        }
    }

    public void swapCursor(Cursor newCursor){
        if(mCursor != null){
            mCursor.close();
        }
        mCursor = newCursor;

        if(newCursor != null){
            notifyDataSetChanged();
        }
    }
}
