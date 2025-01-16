/**
 * 
 */
package nexcore.scheduler.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * <ul>
 * <li>업무 그룹명 : 금융 프레임워크 </li>
 * <li>서브 업무명 : 배치 코어</li>
 * <li>설  명 : webapp 방식으로 설치하는 경우 인코딩을 처리하는 서블릿 필터. </li>
 * <li>작성일 : 2012. 4. 25.</li>
 * <li>작성자 : 정호철</li>
 * </ul>
 */

public class WebCharacterEncodingFilter implements Filter {
	private String encoding;
	

	public void init(FilterConfig config) throws ServletException {
		encoding = config.getInitParameter("encoding");
	}
	
	public void destroy() {
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException {
		request.setCharacterEncoding(encoding);
		fc.doFilter(request, response);
	}
}
