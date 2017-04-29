/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neuronrobotics.bowlerstudio;

/**
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@FunctionalInterface
public interface OutputFilter {
    boolean onMatch(String s);
}
