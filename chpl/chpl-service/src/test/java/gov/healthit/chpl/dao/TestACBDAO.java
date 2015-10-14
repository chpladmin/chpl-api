package gov.healthit.chpl.dao;

import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class TestACBDAO {
	
	@Autowired
	CertificationBodyDAO dao;
	
	@Test
	public void daoIsNotNull(){
		assertNotNull(dao);
	}
	
	@Test
	public void testHash() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		Date now = new Date();
		System.out.println(md5("katy.ekey@gmail.com" + now.getTime()));
	}
	
    public static String md5(String input) {
        
        String md5 = null;
         
        if(null == input) return null;
         
        try {
             
        //Create MessageDigest object for MD5
        MessageDigest digest = MessageDigest.getInstance("MD5");
         
        //Update input string in message digest
        digest.update(input.getBytes(), 0, input.length());
 
        //Converts message digest value in base 16 (hex) 
        md5 = new BigInteger(1, digest.digest()).toString(16);
 
        } catch (NoSuchAlgorithmException e) {
 
            e.printStackTrace();
        }
        return md5;
    }
    
    private static final char[] symbols;

    static {
      StringBuilder tmp = new StringBuilder();
      for (char ch = '0'; ch <= '9'; ++ch)
        tmp.append(ch);
      for (char ch = 'a'; ch <= 'z'; ++ch)
        tmp.append(ch);
      symbols = tmp.toString().toCharArray();
    }   

    private final Random random = new SecureRandom();
    
    @Test
    public void testGeneratePassword() {
    	char[] buf = new char[15];
    	
    	for (int idx = 0; idx < buf.length; ++idx) 
            buf[idx] = symbols[random.nextInt(symbols.length)];
          System.out.println(new String(buf));
    }
}
