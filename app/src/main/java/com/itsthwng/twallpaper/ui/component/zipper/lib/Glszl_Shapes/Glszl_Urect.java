package com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Shapes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations.Glszl_Deplace;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations.Glszl_Fade;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations.Glszl_Rotation;
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_Animations.Glszl_Sizer;

import java.util.ArrayList;
import java.util.List;

public class Glszl_Urect {
    public static List<Glszl_Urect> list = new ArrayList();
    List<ClickDownListner> heart_zipper_ClickDownlisteners;
    List<ClickUpListner> heart_zipper_ClickUplisteners;
    public boolean heart_zipper_Clicked;
    public boolean heart_zipper_DrawChilds;
    List<TouchMoveListner> heart_zipper_TouchMovelisteners;
    List<UpdateListner> heart_zipper_UpdateListners;
    protected double heart_zipper_alpha;
    private List<Glszl_Urect> heart_zipper_childrens;
    public int color;
    Glszl_Deplace heart_zipper_deplace;
    Glszl_Fade heart_zipper_fade;
    protected double heart_zipper_height;
    public Paint heart_zipper_paint;
    private Glszl_Urect heart_zipper_parent;
    protected double heart_zipper_radius;
    protected double heart_zipper_rotate;
    Glszl_Rotation heart_zipper_rotation;
    Glszl_Sizer heart_zipper_sizer;
    public double heart_zipper_skewX;
    public double heart_zipper_skewY;
    protected double heart_zipper_width;

    protected double heart_zipper_f18x;

    protected double heart_zipper_f19y;

    public interface ClickDownListner {
        void OnClickDownDo(double d, double d2);
    }

    public interface ClickUpListner {
        void OnClickUpDo(double d, double d2);
    }

    public interface TouchMoveListner {
        void OnMoveDo(Glszl_Urect urect, double d, double d2);
    }

    public interface UpdateListner {
        void Update(Glszl_Urect urect);
    }

    public static boolean isBetween(double d, double d2, double d3) {
        if (d < d2 || d > d3) {
            return d >= d3 && d <= d2;
        }
        return true;
    }

    public Glszl_Fade getFade() {
        return this.heart_zipper_fade;
    }

    public void setFadeAnnimation(Glszl_Fade fade) {
        Glszl_Fade fade2 = this.heart_zipper_fade;
        if (fade2 != null) {
            fade2.Remove();
        }
        this.heart_zipper_fade = fade;
    }

    public Glszl_Deplace getDeplace() {
        return this.heart_zipper_deplace;
    }

    public void setDeplaceAnnimation(Glszl_Deplace deplace) {
        Glszl_Deplace deplace2 = this.heart_zipper_deplace;
        if (deplace2 != null) {
            deplace2.remove();
        }
        this.heart_zipper_deplace = deplace;
    }

    public Glszl_Rotation getRotation() {
        return this.heart_zipper_rotation;
    }

    public void setRotationAnimation(Glszl_Rotation rotation) {
        this.heart_zipper_rotation = rotation;
    }

    public Glszl_Sizer getSizer() {
        return this.heart_zipper_sizer;
    }

    public void setSizerAnnimation(Glszl_Sizer sizer) {
        this.heart_zipper_sizer = sizer;
    }

    public Glszl_Urect(double d, double d2, double d3, double d4, int i) {
        this(d, d2, d3, d4);
        this.heart_zipper_paint.setColor(i);
        this.color = i;
    }

    public Glszl_Urect(double d, double d2, double d3, double d4) {
        this.heart_zipper_DrawChilds = true;
        this.heart_zipper_childrens = new ArrayList();
        this.heart_zipper_Clicked = false;
        if (list == null) {
            list = new ArrayList();
        }
        this.heart_zipper_f18x = d;
        this.heart_zipper_f19y = d2;
        this.heart_zipper_height = d4;
        this.heart_zipper_width = d3;
        this.heart_zipper_paint = new Paint();
        setAlpha(255.0d);
        this.heart_zipper_paint.setColor(0);
        this.color = 0;
        list.add(this);
    }

    public Glszl_Urect(double d, double d2, double d3, double d4, boolean z) {
        this.heart_zipper_DrawChilds = true;
        this.heart_zipper_childrens = new ArrayList();
        this.heart_zipper_Clicked = false;
        this.heart_zipper_f18x = d;
        this.heart_zipper_f19y = d2;
        this.heart_zipper_height = d4;
        this.heart_zipper_width = d3;
        this.heart_zipper_paint = new Paint();
        setAlpha(255.0d);
        this.heart_zipper_paint.setColor(0);
        this.color = 0;
        if (z) {
            list.add(this);
        }
    }

    public double getRadius() {
        return this.heart_zipper_radius;
    }

    public void setRadius(double d) {
        this.heart_zipper_radius = d;
    }

    public double getLeft() {
        Glszl_Urect urect = this.heart_zipper_parent;
        return urect == null ? this.heart_zipper_f18x : urect.getLeft() + this.heart_zipper_f18x;
    }

    public double getRelativeLeft() {
        return this.heart_zipper_f18x;
    }

    public void setLeft(double d) {
        this.heart_zipper_f18x = d;
    }

    public double getTop() {
        Glszl_Urect urect = this.heart_zipper_parent;
        return urect == null ? this.heart_zipper_f19y : urect.getTop() + this.heart_zipper_f19y;
    }

    public double getRelativeTop() {
        return this.heart_zipper_f19y;
    }

    public void setTop(double d) {
        this.heart_zipper_f19y = d;
    }

    public double getRight() {
        Glszl_Urect urect = this.heart_zipper_parent;
        return (urect == null ? getRelativeLeft() : urect.getLeft() + getRelativeLeft()) + Width();
    }

    public double getRelativeRight() {
        return this.heart_zipper_f18x + Width();
    }

    public void setRight(double d) {
        setLeft(d - Width());
    }

    public double getBottom() {
        Glszl_Urect urect = this.heart_zipper_parent;
        return (urect == null ? this.heart_zipper_f19y : urect.getTop() + this.heart_zipper_f19y) + Height();
    }

    public double getRelativeBottom() {
        return this.heart_zipper_f19y + Height();
    }

    public void setBottom(double d) {
        setTop(d - Height());
    }

    public double Height() {
        return this.heart_zipper_height;
    }

    public void setHeight(double d) {
        this.heart_zipper_height = d;
    }

    public double Width() {
        return this.heart_zipper_width;
    }

    public void setWidth(double d) {
        this.heart_zipper_width = d;
    }

    public double getRotate() {
        return this.heart_zipper_rotate;
    }

    public void setRotate(double d) {
        this.heart_zipper_rotate = d;
    }

    public double getAlpha() {
        Glszl_Urect urect = this.heart_zipper_parent;
        if (urect != null) {
            return (this.heart_zipper_alpha / 255.0d) * urect.getAlpha();
        }
        return this.heart_zipper_alpha;
    }

    public double getRelativeAlpha() {
        return this.heart_zipper_alpha;
    }

    public void setAlpha(double d) {
        this.heart_zipper_alpha = d;
    }

    public int getColor() {
        return this.heart_zipper_paint.getColor();
    }

    public void setColor(int i) {
        this.heart_zipper_paint.setColor(i);
        this.color = i;
    }

    public List<Glszl_Urect> getChildrens() {
        return this.heart_zipper_childrens;
    }

    public void setChildrenCollection(List<Glszl_Urect> list2) {
        this.heart_zipper_childrens = list2;
    }

    public boolean removeParent() {
        if (this.heart_zipper_parent != null) {
            this.heart_zipper_parent = null;
            return true;
        }
        return false;
    }

    public void AddChild(Glszl_Urect urect) {
        if (this.heart_zipper_childrens == null) {
            this.heart_zipper_childrens = new ArrayList();
        }
        this.heart_zipper_childrens.add(urect);
        urect.heart_zipper_parent = this;
    }

    public boolean DeleteChild(Glszl_Urect urect) {
        if (urect.getParent() != this) {
            return false;
        }
        List<Glszl_Urect> list2 = this.heart_zipper_childrens;
        if (list2 != null) {
            list2.remove(urect);
        }
        return urect.removeParent();
    }

    public Glszl_Urect getParent() {
        return this.heart_zipper_parent;
    }

    public void setParent(Glszl_Urect urect) {
        if (urect != null) {
            urect.getChildrens().remove(this);
        }
        this.heart_zipper_parent = urect;
        urect.AddChild(this);
    }

    public Rect GetRect() {
        return new Rect((int) getLeft(), (int) getTop(), (int) getRight(), (int) getBottom());
    }

    public RectF GetRectF() {
        return new RectF((float) getLeft(), (float) getTop(), (float) getRight(), (float) getBottom());
    }

    public double GetCenterX() {
        return (getLeft() + getRight()) / 2.0d;
    }

    public double getCenterY() {
        return (getTop() + getBottom()) / 2.0d;
    }

    public void Draw(Canvas canvas) {
        int save = canvas.save();
        canvas.rotate((int) getRotate(), (int) GetCenterX(), (int) getCenterY());
        canvas.skew((int) this.heart_zipper_skewX, (int) this.heart_zipper_skewY);
        this.heart_zipper_paint.setAlpha((int) getAlpha());
        if (this.color != 0) {
            if (getRadius() == 0.0d) {
                canvas.drawRect(GetRect(), this.heart_zipper_paint);
            } else {
                RectF GetRectF = GetRectF();
                double d = this.heart_zipper_radius;
                canvas.drawRoundRect(GetRectF, (float) d, (float) d, this.heart_zipper_paint);
            }
        }
        canvas.restoreToCount(save);
        drawChildrens(canvas);
    }

    public void drawChildrens(Canvas canvas) {
        if (!this.heart_zipper_DrawChilds || this.heart_zipper_childrens == null) {
            return;
        }
        for (int i = 0; i < this.heart_zipper_childrens.size(); i++) {
            if (this.heart_zipper_childrens.get(i) != null) {
                this.heart_zipper_childrens.get(i).Draw(canvas);
            }
        }
    }

    public boolean IsClicked(double d, double d2) {
        return d >= getLeft() && d <= getRight() && d2 >= getTop() && d2 <= getBottom();
    }

    public void addOnClickDownListner(ClickDownListner clickDownListner) {
        if (this.heart_zipper_ClickDownlisteners == null) {
            this.heart_zipper_ClickDownlisteners = new ArrayList();
        }
        this.heart_zipper_ClickDownlisteners.add(clickDownListner);
    }

    public void addOnClickUpListner(ClickUpListner clickUpListner) {
        if (this.heart_zipper_ClickUplisteners == null) {
            this.heart_zipper_ClickUplisteners = new ArrayList();
        }
        this.heart_zipper_ClickUplisteners.add(clickUpListner);
    }

    public void addOnTouchMoveListner(TouchMoveListner touchMoveListner) {
        if (this.heart_zipper_TouchMovelisteners == null) {
            this.heart_zipper_TouchMovelisteners = new ArrayList();
        }
        this.heart_zipper_TouchMovelisteners.add(touchMoveListner);
    }

    public void OnUpdateListner(UpdateListner updateListner) {
        if (this.heart_zipper_UpdateListners == null) {
            this.heart_zipper_UpdateListners = new ArrayList();
        }
        this.heart_zipper_UpdateListners.add(updateListner);
    }

    public void removeOnClickDownListner(ClickDownListner clickDownListner) {
        List<ClickDownListner> list2 = this.heart_zipper_ClickDownlisteners;
        if (list2 != null) {
            list2.remove(clickDownListner);
        }
    }

    public void removeOnClickUpListner(ClickUpListner clickUpListner) {
        List<ClickUpListner> list2 = this.heart_zipper_ClickUplisteners;
        if (list2 != null) {
            list2.remove(clickUpListner);
        }
    }

    public void removeOnTouchMoveListner(TouchMoveListner touchMoveListner) {
        List<TouchMoveListner> list2 = this.heart_zipper_TouchMovelisteners;
        if (list2 != null) {
            list2.remove(touchMoveListner);
        }
    }

    public void removeUpdateListner(UpdateListner updateListner) {
        this.heart_zipper_UpdateListners.remove(updateListner);
    }

    public static void CheckRectsClickUp() {
        for (int size = list.size() - 1; size >= 0; size--) {
            list.get(size).checkClickUp();
        }
    }

    public static void CheckRectTouchMove(double d, double d2) {
        for (int size = list.size() - 1; size >= 0; size--) {
            try {
                list.get(size).checkTouchMove(d, d2);
            } catch (Exception unused) {
            }
        }
    }

    public static void CheckUpdates() {
        for (int size = list.size() - 1; size >= 0; size--) {
            try {
                list.get(size).CheckObjUpdates();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkClickDown(double d, double d2) {
        for (int size = this.heart_zipper_childrens.size() - 1; size >= 0; size--) {
            if (this.heart_zipper_childrens.get(size).checkClickDown(d, d2)) {
                return true;
            }
        }
        int i = 0;
        if (!IsClicked(d, d2) || this.heart_zipper_ClickDownlisteners == null) {
            return false;
        }
        boolean z = false;
        while (i < this.heart_zipper_ClickDownlisteners.size()) {
            this.heart_zipper_ClickDownlisteners.get(i).OnClickDownDo(d, d2);
            i++;
            z = true;
        }
        this.heart_zipper_Clicked = true;
        return z;
    }

    public void checkClickUp() {
        if (!this.heart_zipper_Clicked || this.heart_zipper_ClickUplisteners == null) {
            return;
        }
        for (int i = 0; i < this.heart_zipper_ClickUplisteners.size(); i++) {
            this.heart_zipper_ClickUplisteners.get(i).OnClickUpDo(this.heart_zipper_f18x, this.heart_zipper_f19y);
        }
        this.heart_zipper_Clicked = false;
    }

    public void checkTouchMove(double d, double d2) {
        if (this.heart_zipper_TouchMovelisteners != null) {
            for (int i = 0; i < this.heart_zipper_TouchMovelisteners.size(); i++) {
                this.heart_zipper_TouchMovelisteners.get(i).OnMoveDo(this, d, d2);
            }
        }
    }

    public void CheckObjUpdates() {
        List<UpdateListner> list2 = this.heart_zipper_UpdateListners;
        if (list2 != null) {
            for (int size = list2.size() - 1; size >= 0; size--) {
                this.heart_zipper_UpdateListners.get(size).Update(this);
            }
        }
        List<Glszl_Urect> list3 = this.heart_zipper_childrens;
        if (list3 != null) {
            for (int size2 = list3.size() - 1; size2 >= 0; size2--) {
                this.heart_zipper_childrens.get(size2).CheckObjUpdates();
            }
        }
    }

    public boolean isCollide(Glszl_Urect urect) {
        if ((urect.getLeft() <= getLeft() || urect.getLeft() > getRight()) && (urect.getRight() <= getLeft() || urect.getRight() >= getRight())) {
            return false;
        }
        if (urect.getTop() <= getTop() || urect.getTop() > getBottom()) {
            return urect.getBottom() > getTop() && urect.getBottom() < getBottom();
        }
        return true;
    }

    public void Delete() {
        Glszl_Urect urect = this.heart_zipper_parent;
        if (urect != null) {
            urect.DeleteChild(this);
        }
        List<Glszl_Urect> list2 = list;
        if (list2 != null) {
            list2.remove(this);
        }
        List<ClickDownListner> list3 = this.heart_zipper_ClickDownlisteners;
        if (list3 != null) {
            list3.clear();
        }
        List<ClickUpListner> list4 = this.heart_zipper_ClickUplisteners;
        if (list4 != null) {
            list4.clear();
        }
        List<TouchMoveListner> list5 = this.heart_zipper_TouchMovelisteners;
        if (list5 != null) {
            list5.clear();
        }
        List<UpdateListner> list6 = this.heart_zipper_UpdateListners;
        if (list6 != null) {
            list6.clear();
        }
    }

    public void clearChilds() {
        for (int i = 0; i < this.heart_zipper_childrens.size(); i++) {
            this.heart_zipper_childrens.get(i).Delete();
        }
    }
}
