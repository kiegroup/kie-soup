package org.kie.soup.project.datamodel;

import org.junit.Test;
import org.kie.soup.project.datamodel.oracle.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AnnotationTest {

    @Test
    public void testEqualsAndHashCode() {
        Annotation annotation1 = new Annotation();
        Annotation annotation2 = new Annotation();
        assertTrue(annotation1.equals(annotation2));
        assertEquals(annotation1.hashCode(), annotation2.hashCode());

        annotation1 = new Annotation("someName");
        annotation2 = new Annotation("someName");
        assertTrue(annotation1.equals(annotation2));
        assertEquals(annotation1.hashCode(), annotation2.hashCode());

        annotation1.addParameter("param1", "value1");
        annotation2.addParameter("param1", "value1");
        assertTrue(annotation1.equals(annotation2));
        assertEquals(annotation1.hashCode(), annotation2.hashCode());

        annotation1.addParameter("param2", "value2");
        annotation2.addParameter("param2", "value2");
        assertTrue(annotation1.equals(annotation2));
        assertEquals(annotation1.hashCode(), annotation2.hashCode());

        annotation1 = new Annotation("someName");
        annotation2 = new Annotation("someName1");
        assertFalse(annotation1.equals(annotation2));
        assertNotEquals(annotation1.hashCode(), annotation2.hashCode());

        annotation1 = new Annotation("someName");
        annotation2 = new Annotation("someName");
        annotation1.addParameter("param1", "value1");
        assertFalse(annotation1.equals(annotation2));
        assertNotEquals(annotation1.hashCode(), annotation2.hashCode());
    }
}
