/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    WekaEnumeration.java
 *    Copyright (C) 2009-2012 University of Waikato, Hamilton, New Zealand
 *
 */
package weka.core;

import java.util.Enumeration;
import java.util.List;

/**
 * Class for enumerating an array list's elements.
 */
public class MyWekaEnumeration<E> implements Enumeration<E>, RevisionHandler {

  /** The counter. */
  private int m_Counter;
  // These JML commands say how m_Counter implements Enumeration
  // @ in moreElements;
  // @ private represents moreElements = m_Counter < m_Vector.size();
  // @ private invariant 0 <= m_Counter && m_Counter <= m_Vector.size();

  /** The vector. */
  private /* @non_null@ */E[] m_Vector;
  private /* @non_null@ */int m_pos;

  // @ private invariant -1 <= m_SpecialElement;
  // @ private invariant m_SpecialElement < m_Vector.size();
  // @ private invariant m_SpecialElement>=0 ==> m_Counter!=m_SpecialElement;

  /**
   * Constructs an enumeration.
   * 
   * @param vector the vector which is to be enumerated
   */
  public MyWekaEnumeration(/* @non_null@ */E[] vector, int pos) {
    m_Counter = 0;
    m_pos = pos;
    m_Vector = vector;
  }

  /**
   * Tests if there are any more elements to enumerate.
   * 
   * @return true if there are some elements left
   */
  @Override
  public final/* @pure@ */boolean hasMoreElements() {

    if (m_Counter < m_pos) {
      return true;
    }
    return false;
  }

  /**
   * Returns the next element.
   * 
   * @return the next element to be enumerated
   */
  // @ also requires hasMoreElements();
  @Override
  public final E nextElement() {
    E result = m_Vector[m_Counter];
    m_Counter++;
    return result;
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 10203 $");
  }
}
