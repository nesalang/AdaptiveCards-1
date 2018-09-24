package io.adaptivecards.renderer.readonly;

import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import io.adaptivecards.objectmodel.ContainerStyle;
import io.adaptivecards.objectmodel.HeightType;
import io.adaptivecards.objectmodel.MarkDownParser;
import io.adaptivecards.renderer.RenderedAdaptiveCard;
import io.adaptivecards.renderer.Util;
import io.adaptivecards.renderer.actionhandler.ICardActionHandler;
import io.adaptivecards.renderer.readonly.TextBlockRenderer.UlTagHandler;
import io.adaptivecards.renderer.inputhandler.IInputHandler;
import io.adaptivecards.objectmodel.BaseCardElement;
import io.adaptivecards.objectmodel.Fact;
import io.adaptivecards.objectmodel.FactVector;
import io.adaptivecards.objectmodel.HostConfig;
import io.adaptivecards.objectmodel.FactSet;
import io.adaptivecards.objectmodel.TextConfig;
import io.adaptivecards.renderer.BaseCardElementRenderer;

import java.util.Vector;

public class FactSetRenderer extends BaseCardElementRenderer
{
    protected FactSetRenderer()
    {
    }

    public static FactSetRenderer getInstance()
    {
        if (s_instance == null)
        {
            s_instance = new FactSetRenderer();
        }

        return s_instance;
    }

    static TextView createTextView(Context context, CharSequence text, TextConfig textConfig, HostConfig hostConfig, long spacing, ContainerStyle containerStyle)
    {
        TextView textView = new TextView(context);
        textView.setText(text);

        TextBlockRenderer.setTextColor(textView, textConfig.getColor(), hostConfig, textConfig.getIsSubtle(), containerStyle);
        TextBlockRenderer.setTextSize(context, textView, textConfig.getSize(), hostConfig);
        TextBlockRenderer.getInstance().setTextFormat(textView, hostConfig.getFontFamily(), textConfig.getWeight());
        textView.setSingleLine(!textConfig.getWrap());
        textView.setMaxWidth(Util.dpToPixels(context, textConfig.getMaxWidth()));
        textView.setEllipsize(TextUtils.TruncateAt.END);

        textView.setPaddingRelative(0, 0, (int)spacing,0);
        return textView;
    }

    @Override
    public View render(
            RenderedAdaptiveCard renderedCard,
            Context context,
            FragmentManager fragmentManager,
            ViewGroup viewGroup,
            BaseCardElement baseCardElement,
            ICardActionHandler cardActionHandler,
            HostConfig hostConfig,
            ContainerStyle containerStyle)
    {
        FactSet factSet = null;
        if (baseCardElement instanceof FactSet)
        {
            factSet = (FactSet) baseCardElement;
        }
        else if ((factSet = FactSet.dynamic_cast(baseCardElement)) == null)
        {
            throw new InternalError("Unable to convert BaseCardElement to FactSet object model.");
        }

        setSpacingAndSeparator(context, viewGroup, factSet.GetSpacing(), factSet.GetSeparator(), hostConfig, true);

        TableLayout tableLayout = new TableLayout(context);
        tableLayout.setTag(factSet);
        tableLayout.setColumnShrinkable(1, true);
        HeightType height = factSet.GetHeight();

        if(height == HeightType.Stretch)
        {
            tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.MATCH_PARENT, 1));
        }
        else
        {
            tableLayout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }

        FactVector factVector = factSet.GetFacts();
        long factVectorSize = factVector.size();
        long spacing = hostConfig.getFactSet().getSpacing();

        for (int i = 0; i < factVectorSize; i++)
        {
            Fact fact = factVector.get(i);
            TableRow factRow = new TableRow(context);

            if( height == HeightType.Stretch )
            {
                factRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT, 1));
            }
            else
            {
                factRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            }

            factRow.addView(createTextView(context, fact.GetTitle(), hostConfig.getFactSet().getTitle(), hostConfig, spacing, containerStyle));
            factRow.addView(createTextView(context, HandleValueSpecialText(fact), hostConfig.getFactSet().getValue(), hostConfig, 0, containerStyle));

            tableLayout.addView(factRow);
        }

        viewGroup.addView(tableLayout);
        return tableLayout;
    }

    private CharSequence HandleValueSpecialText(Fact fact)
    {
      DateTimeParser parser = new DateTimeParser(fact.GetLanguage());
      String textWithFormattedDates = parser.GenerateString(fact.GetValueForDateParsing());

      MarkDownParser markDownParser = new MarkDownParser(textWithFormattedDates);
      String textString = markDownParser.TransformToHtml();
      Spanned htmlString;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
      {
          htmlString = Html.fromHtml(textString, Html.FROM_HTML_MODE_COMPACT, null, new UlTagHandler());
      }
      else
      {
          // Before Android N, html.fromHtml adds two newline characters to end of string
          htmlString = Html.fromHtml(textString, null, new UlTagHandler());
      }
      return trimHtmlString(htmlString);
    }

    private CharSequence trimHtmlString(Spanned htmlString)
    {
        int numToRemoveFromEnd = 0;
        int numToRemoveFromStart = 0;

        for (int i = htmlString.length()-1; i >= 0; --i)
        {
            if (htmlString.charAt(i) == '\n')
            {
                numToRemoveFromEnd++;
            }
            else
            {
                break;
            }
        }

        for (int i = 0; i <= htmlString.length()-1; ++i)
        {
            if (htmlString.charAt(i) == '\n')
            {
                numToRemoveFromStart++;
            }
            else
            {
                break;
            }
        }

        //Sanity check
        if (numToRemoveFromStart + numToRemoveFromEnd >= htmlString.length())
        {
            return htmlString;
        }

        return htmlString.subSequence(numToRemoveFromStart, htmlString.length()-numToRemoveFromEnd);
    }

    private static FactSetRenderer s_instance = null;
}
