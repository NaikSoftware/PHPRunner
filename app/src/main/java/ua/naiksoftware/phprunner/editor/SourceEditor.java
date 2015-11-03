package ua.naiksoftware.phprunner.editor;

import android.text.*;

import android.content.Context;
import android.os.Handler;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ua.naiksoftware.phprunner.EditorActivity;
import ua.naiksoftware.phprunner.editor.language.Language;
import ua.naiksoftware.phprunner.editor.language.PHP;
import ua.naiksoftware.phprunner.editor.language.PatternColorPair;

public class SourceEditor extends EditText {

    private Context ctx;
    private CodeChangedListener cclistener;
    public int updateDelay = 1000;
    private Language language;
    public boolean dirty = false;
//    private static final int COLOR_ERROR = 0x80ff0000;
    public static final int COLOR_LITERAL = 0xff7ba212;
    public static final int COLOR_KEYWORD = 0xffCC4343;
    public static final int COLOR_BUILTIN = 0xff6666FF;
    public static final int COLOR_COMMENT = 0xff808080;
    public static final int COLOR_VAR = 0xff5544AA;
//    private static final Pattern line = Pattern.compile(".*\\n");
//    private static final Pattern trailingWhiteSpace = Pattern.compile("[\\t ]+$", Pattern.MULTILINE);
    private final Handler updateHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Editable e = getText();
            highlightWithoutChange(e);
        }
    };
    private boolean modified = true;

    public SourceEditor(Context context) {
        super(context);
        init(context);
    }

    public SourceEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Code changed here.
    public void setTextHighlighted(CharSequence text, int codeType) {
        switch (codeType) {
            case EditorActivity.PHP:
                language = new PHP();
                break;
            case EditorActivity.HTML:
                //language = new HTML();
                break;
        }
        cancelUpdate();
        dirty = false;

        modified = false;
        setText(highlight(new SpannableStringBuilder(text)));
        modified = true;
    }

    private void init(Context ctx) {
        this.ctx = ctx;
        setHorizontallyScrolling(false);

        setFilters(new InputFilter[]{
            new InputFilter() {
                @Override
                public CharSequence filter(
                        CharSequence source,
                        int start,
                        int end,
                        Spanned dest,
                        int dstart,
                        int dend) {
                    if (modified
                            && end - start == 1
                            && start < source.length()
                            && dstart < dest.length()) {
                        char c = source.charAt(start);
                        if (c == '\n') {
                            return autoIndent(source, start, end, dest, dstart, dend);
                        }
                    }
                    return source;
                }
            }});

        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                cancelUpdate();

                if (!modified) {
                    return;
                }

                dirty = true;
                updateHandler.postDelayed(
                        updateRunnable,
                        updateDelay);
                if (cclistener != null) {
                    cclistener.codeChanged();
                }
            }
        });
    }

    private void cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable);
    }

    private void highlightWithoutChange(Editable e) {
        modified = false;
        highlight(e);
        modified = true;
    }

    private Editable highlight(Editable e) {
        try {
            // don't use e.clearSpans() because it will remove
            // too much
            clearSpans(e);

            if (e.length() == 0) {
                return e;
            }
            Pattern pattern;
            int color;
            for (PatternColorPair pair : language.getPatternColorPairs()) {
                pattern = pair.getPattern();
                color = pair.getColor();
                for (Matcher m = pattern.matcher(e); m.find();) {
                    e.setSpan(
                            new ForegroundColorSpan(color),
                            m.start(),
                            m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        } catch (Exception ex) {
        }

        return e;
    }

    private void clearSpans(Editable e) {
        // remove foreground color spans
        {
            ForegroundColorSpan spans[] = e.getSpans(0, e.length(), ForegroundColorSpan.class);
            for (int n = spans.length; n-- > 0;) {
                e.removeSpan(spans[n]);
            }
        }
        // remove background color spans
        {
            BackgroundColorSpan spans[] = e.getSpans(0, e.length(), BackgroundColorSpan.class);
            for (int n = spans.length; n-- > 0;) {
                e.removeSpan(spans[n]);
            }
        }
    }

    private CharSequence autoIndent(
            CharSequence source,
            int start,
            int end,
            Spanned dest,
            int dstart,
            int dend) {
        String indent = "";
        int istart = dstart - 1;
        int iend = -1;

        // find start of this line
        boolean dataBefore = false;
        int pt = 0;

        for (; istart > -1; --istart) {
            char c = dest.charAt(istart);

            if (c == '\n') {
                break;
            }

            if (c != ' '
                    && c != '\t') {
                if (!dataBefore) {
                    // indent always after those characters
                    if (c == '{'
                            || c == '+'
                            || c == '-'
                            || c == '*'
                            || c == '/'
                            || c == '%'
                            || c == '^'
                            || c == '=') {
                        --pt;
                    }

                    dataBefore = true;
                }

                // parenthesis counter
                if (c == '(') {
                    --pt;
                } else if (c == ')') {
                    ++pt;
                }
            }
        }

        // copy indent of this line into the next
        if (istart > -1) {
            char charAtCursor = dest.charAt(dstart);

            for (iend = ++istart;
                    iend < dend;
                    ++iend) {
                char c = dest.charAt(iend);

                // auto expand comments
                if (charAtCursor != '\n'
                        && c == '/'
                        && iend + 1 < dend
                        && dest.charAt(iend) == c) {
                    iend += 2;
                    break;
                }

                if (c != ' '
                        && c != '\t') {
                    break;
                }
            }

            indent += dest.subSequence(istart, iend);
        }

        // add new indent
        if (pt < 0) {
            indent += "\t";
        }

        // append white space of previous line and new indent
        return source + indent;
    }

    public void setCodeChangedListener(CodeChangedListener cclistener) {
        this.cclistener = cclistener;
    }
}
