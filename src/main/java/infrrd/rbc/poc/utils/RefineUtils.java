package infrrd.rbc.poc.utils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;


@Component
@PropertySource(value = { "classpath:project.properties" })
public class RefineUtils {

	@Value("${ignoreKeyWords}")
	String ignoreKeyWords;
	
	
	@Value("${keywordsToMap}")
	String keywordsToMap;
	
	
	public String refineThis(String string) {
		String[] inputList = string.replaceAll( "[\n]", " " ).split( "\\W+" );
		List<String> spellWordsIgnore = Arrays.asList(ignoreKeyWords.split(","));
		
		
		
		List<String> spellWords =  Arrays.asList(keywordsToMap.split(","));
		NormalizedLevenshtein l = new NormalizedLevenshtein();
		for ( int i = 0; i < inputList.length; i++ ) {
            String inputWord = inputList[i].trim().toLowerCase();

            // if contain in ignore list then ignore while PREPROCESSING and conitnue
            if ( spellWordsIgnore.contains( inputWord ) || spellWords.contains( inputWord ) ) {
                continue;
            }
            double threshhold =  0.3;
            
            if ( inputWord.matches( "[a-zA-Z0-9]+" ) ) {
            	String closest = "";
            	// no shortest distance found, yet
                double shortest = -1;
                // loop through words to find the closest
                for ( String word : spellWords ) {
                    // calculate the distance between the input word, and the current word
                    double lev = l.distance( inputWord, word );
                    // if this distance is less than the next found shortest distance, OR if a next shortest word has not yet been found
                    if ( lev <= shortest || shortest < 0 ) {
                        // set the closest match, and shortest distance
                        closest = word;
                        shortest = lev;
                    }
                }

                if ( shortest <= threshhold && StringUtils.isNotEmpty(closest)) {
                	string = string.replaceAll("(?i)[ ]"+inputWord +"[ ]",closest.toLowerCase() );
                }
            	
            	
            }
		}
		
		
		return string;
	}

	
	
	
	
	
	
}
