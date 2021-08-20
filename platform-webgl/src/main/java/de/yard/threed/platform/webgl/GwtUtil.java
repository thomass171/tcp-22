package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created by thomass on 15.05.15.
 */
public class GwtUtil {
    /**
     *
     */
    public static void showErrorDialogBox(String message) {
        final DialogBox dialogBox = createDialogBox("Error",message);
        dialogBox.setGlassEnabled(true);
        dialogBox.setAnimationEnabled(true);
        dialogBox.center();
        dialogBox.show();
    }

    /**
     * Create the dialog box for this example.
     *
     * @return the new dialog box
     */
    private static DialogBox createDialogBox(String caption, String message) {
        // Create a dialog box and set the caption text
        final DialogBox dialogBox = new DialogBox();
        dialogBox.ensureDebugId("cwDialogBox");
        dialogBox.setText(caption);

        // Create a table to layout the content
        VerticalPanel dialogContents = new VerticalPanel();
        dialogContents.setSpacing(4);
        dialogBox.setWidget(dialogContents);

        // Add some text to the top of the dialog
        HTML details = new HTML(message);
        dialogContents.add(details);
        dialogContents.setCellHorizontalAlignment(
                details, HasHorizontalAlignment.ALIGN_CENTER);


        // Add a close button at the bottom of the dialog
        Button closeButton = new Button(
                "Close", new ClickHandler() {
            public void onClick(ClickEvent event) {
                //TODO muss die nicht richtig entfernt werden?
                dialogBox.hide();
            }
        });
        dialogContents.add(closeButton);

        // Return the dialog box
        return dialogBox;
    }

    public static native boolean hasProperty(JavaScriptObject jso,String key)  /*-{
        var hp = jso.hasOwnProperty(key);
        return hp;
    }-*/;

    public static native String getName(JavaScriptObject jso)  /*-{
        var name = jso.name;
        return name;
    }-*/;

    static native String getType(JavaScriptObject jso)  /*-{
        return jso.type;
    }-*/;

    static native void showStatus(String s)  /*-{
    //alert(s);
        var statusfield = $wnd.document.getElementById("statusfield");
        if (statusfield != null) {
		    statusfield.innerHTML = s;
		}
		//alert(s);
    }-*/;
}
