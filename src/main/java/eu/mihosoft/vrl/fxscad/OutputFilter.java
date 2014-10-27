/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.mihosoft.vrl.fxscad;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@FunctionalInterface
public interface OutputFilter {
    public boolean onMatch(String s);
}
