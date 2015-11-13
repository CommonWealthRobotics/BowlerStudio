/*
 * Copyright (c) 2011, 2013 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.neuronrobotics.bowlerstudio.threed;

import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

// TODO: Auto-generated Javadoc
/**
 * The Class Xform.
 */
public class Xform extends Group {

    /**
     * The Enum RotateOrder.
     */
    public enum RotateOrder {
        
        /** The xyz. */
        XYZ, 
 /** The xzy. */
 XZY, 
 /** The yxz. */
 YXZ, 
 /** The yzx. */
 YZX, 
 /** The zxy. */
 ZXY, 
 /** The zyx. */
 ZYX
    }

    /** The t. */
    public Translate t  = new Translate(); 
    
    /** The p. */
    public Translate p  = new Translate(); 
    
    /** The ip. */
    public Translate ip = new Translate(); 
    
    /** The rx. */
    public Rotate rx = new Rotate();
    { rx.setAxis(Rotate.X_AXIS); }
    
    /** The ry. */
    public Rotate ry = new Rotate();
    { ry.setAxis(Rotate.Y_AXIS); }
    
    /** The rz. */
    public Rotate rz = new Rotate();
    { rz.setAxis(Rotate.Z_AXIS); }
    
    /** The s. */
    public Scale s = new Scale();

    /**
     * Instantiates a new xform.
     */
    public Xform() { 
        super(); 
        getTransforms().addAll(t, rz, ry, rx, s); 
    }

    /**
     * Instantiates a new xform.
     *
     * @param rotateOrder the rotate order
     */
    public Xform(RotateOrder rotateOrder) { 
        super(); 
        // choose the order of rotations based on the rotateOrder
        switch (rotateOrder) {
        case XYZ:
            getTransforms().addAll(t, p, rz, ry, rx, s, ip); 
            break;
        case XZY:
            getTransforms().addAll(t, p, ry, rz, rx, s, ip); 
            break;
        case YXZ:
            getTransforms().addAll(t, p, rz, rx, ry, s, ip); 
            break;
        case YZX:
            getTransforms().addAll(t, p, rx, rz, ry, s, ip);  // For Camera
            break;
        case ZXY:
            getTransforms().addAll(t, p, ry, rx, rz, s, ip); 
            break;
        case ZYX:
            getTransforms().addAll(t, p, rx, ry, rz, s, ip); 
            break;
        }
    }

    /**
     * Sets the translate.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void setTranslate(double x, double y, double z) {
        t.setX(x);
        t.setY(y);
        t.setZ(z);
    }

    /**
     * Sets the translate.
     *
     * @param x the x
     * @param y the y
     */
    public void setTranslate(double x, double y) {
        t.setX(x);
        t.setY(y);
    }

    // Cannot override these methods as they are final:
    // public void setTranslateX(double x) { t.setX(x); }
    // public void setTranslateY(double y) { t.setY(y); }
    // public void setTranslateZ(double z) { t.setZ(z); }
    /**
     * Sets the tx.
     *
     * @param x the new tx
     */
    // Use these methods instead:
    public void setTx(double x) { t.setX(x); }
    
    /**
     * Sets the ty.
     *
     * @param y the new ty
     */
    public void setTy(double y) { t.setY(y); }
    
    /**
     * Sets the tz.
     *
     * @param z the new tz
     */
    public void setTz(double z) { t.setZ(z); }

    /**
     * Sets the rotate.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void setRotate(double x, double y, double z) {
        rx.setAngle(x);
        ry.setAngle(y);
        rz.setAngle(z);
    }

    /**
     * Sets the rotate x.
     *
     * @param x the new rotate x
     */
    public void setRotateX(double x) { rx.setAngle(x); }
    
    /**
     * Sets the rotate y.
     *
     * @param y the new rotate y
     */
    public void setRotateY(double y) { ry.setAngle(y); }
    
    /**
     * Sets the rotate z.
     *
     * @param z the new rotate z
     */
    public void setRotateZ(double z) { rz.setAngle(z); }
    
    /**
     * Sets the rx.
     *
     * @param x the new rx
     */
    public void setRx(double x) { rx.setAngle(x); }
    
    /**
     * Sets the ry.
     *
     * @param y the new ry
     */
    public void setRy(double y) { ry.setAngle(y); }
    
    /**
     * Sets the rz.
     *
     * @param z the new rz
     */
    public void setRz(double z) { rz.setAngle(z); }

    /**
     * Sets the scale.
     *
     * @param scaleFactor the new scale
     */
    public void setScale(double scaleFactor) {
        s.setX(scaleFactor);
        s.setY(scaleFactor);
        s.setZ(scaleFactor);
    }

    /**
     * Sets the scale.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void setScale(double x, double y, double z) {
        s.setX(x);
        s.setY(y);
        s.setZ(z);
    }

    // Cannot override these methods as they are final:
    // public void setScaleX(double x) { s.setX(x); }
    // public void setScaleY(double y) { s.setY(y); }
    // public void setScaleZ(double z) { s.setZ(z); }
    /**
     * Sets the sx.
     *
     * @param x the new sx
     */
    // Use these methods instead:
    public void setSx(double x) { s.setX(x); }
    
    /**
     * Sets the sy.
     *
     * @param y the new sy
     */
    public void setSy(double y) { s.setY(y); }
    
    /**
     * Sets the sz.
     *
     * @param z the new sz
     */
    public void setSz(double z) { s.setZ(z); }

    /**
     * Sets the pivot.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     */
    public void setPivot(double x, double y, double z) {
        p.setX(x);
        p.setY(y);
        p.setZ(z);
        ip.setX(-x);
        ip.setY(-y);
        ip.setZ(-z);
    }

    /**
     * Reset.
     */
    public void reset() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        rx.setAngle(0.0);
        ry.setAngle(0.0);
        rz.setAngle(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }

    /**
     * Reset tsp.
     */
    public void resetTSP() {
        t.setX(0.0);
        t.setY(0.0);
        t.setZ(0.0);
        s.setX(1.0);
        s.setY(1.0);
        s.setZ(1.0);
        p.setX(0.0);
        p.setY(0.0);
        p.setZ(0.0);
        ip.setX(0.0);
        ip.setY(0.0);
        ip.setZ(0.0);
    }
}
