package com.tyron.code.ui.editor.log.adapter;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.tyron.builder.model.DiagnosticWrapper;
import com.tyron.code.R;

import javax.tools.Diagnostic;

import java.util.Locale;

public class LogAdapter extends ListAdapter<DiagnosticWrapper, LogAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(DiagnosticWrapper diagnostic);
    }

    private OnClickListener mListener;

    public LogAdapter() {
        super(new DiffCallback());
    }

    public void setListener(OnClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.log_text_view);
        }

        public void bind(DiagnosticWrapper diagnostic) {
            if (diagnostic == null || diagnostic.getMessage(Locale.getDefault()) == null) {
                textView.setText("");
                return;
            }

            SpannableStringBuilder builder = new SpannableStringBuilder();
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                builder.append(diagnostic.getMessage(Locale.getDefault()),
                        new ForegroundColorSpan(getColor(diagnostic.getKind())),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                builder.append(diagnostic.getMessageCharSequence());
            }

            if (diagnostic.getSource() != null) {
                builder.append(' ');
                addClickableFile(builder, diagnostic);
            }

            textView.setText(builder);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        @ColorInt
        private int getColor(Diagnostic.Kind kind) {
            switch (kind) {
                case ERROR:
                    return 0xffcf6679;
                case MANDATORY_WARNING:
                case WARNING:
                    return Color.YELLOW;
                case NOTE:
                    return Color.CYAN;
                default:
                    return 0xffFFFFFF;
            }
        }

        private void addClickableFile(SpannableStringBuilder sb, final DiagnosticWrapper diagnostic) {
            if (diagnostic.getSource() == null || !diagnostic.getSource().exists()) {
                return;
            }

            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if (diagnostic.getOnClickListener() != null) {
                        diagnostic.getOnClickListener().onClick(widget);
                    }
                }
            };

            String label = diagnostic.getSource().getName() + ":" + diagnostic.getLineNumber();
            sb.append("[").append(label).append("]", span, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static class DiffCallback extends DiffUtil.ItemCallback<DiagnosticWrapper> {
        @Override
        public boolean areItemsTheSame(@NonNull DiagnosticWrapper oldItem, @NonNull DiagnosticWrapper newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull DiagnosticWrapper oldItem, @NonNull DiagnosticWrapper newItem) {
            return oldItem.equals(newItem);
        }
    }
}
