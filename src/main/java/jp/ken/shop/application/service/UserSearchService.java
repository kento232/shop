package jp.ken.shop.application.service;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jp.ken.shop.domain.entity.UserEntity;
import jp.ken.shop.domain.repository.UserRepository;


@Service
	public class UserSearchService implements UserDetailsService {
		private  UserRepository userRepository;
		
		public UserSearchService(UserRepository userRepository) {
			this.userRepository = userRepository;
		}
		@Override
		public UserDetails loadUserByUsername(String input) 
				throws UsernameNotFoundException {
			
			// ユーザー名（ID/メール）が未入力の場合 
			if (input == null || input.isEmpty()) { 
				throw new UsernameNotFoundException("会員ID、もしくはメールアドレス、パスワードは必須入力です。");
			}
		
			
			List<UserEntity> list;
			//入力が数字だけなら「会員ID」で検索
			if (input.matches("\\d+")) {
				list = userRepository.findByMemberId(Integer.parseInt(input));
			}
			//それ以外は「メールアドレス」で検索
			else {
				list = userRepository.findByMemberEmail(input);
			}
			
			// 0件ならログイン失敗 
			if (list.isEmpty()) {
				throw new UsernameNotFoundException("User not found: " + input);
			}
			// 1件目を取り出す 
			UserEntity user = list.get(0);
			
			
			// UserSearchService  #loadUserByUsername の戻り値
			return User.withUsername(user.getMemberMail())   // username はメールで統一
					.password(user.getMemberPassword())      // DB のパスワード
					.roles("USER")
					.build();
		
		}
}

