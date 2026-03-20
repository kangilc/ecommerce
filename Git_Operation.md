상황별로 **가장 많이 쓰는 commit 롤백 방법**을 정리해 드리겠습니다.  
(실무 기준으로 **언제 어떤 걸 써야 하는지** 위주)

***

## 1️⃣ 아직 **push 안 한 commit** 되돌리기 (로컬에서만)

### ✅ 가장 안전 / 많이 쓰는 방법

### 🔹 마지막 commit 취소 + 변경 내용은 유지

```bash
git reset --soft HEAD~1
```

*   마지막 commit만 취소
*   수정한 파일들은 **staging 상태 유지**
*   👉 커밋 메시지만 잘못 썼을 때 최고

***

### 🔹 마지막 commit 취소 + staging 해제

```bash
git reset --mixed HEAD~1
# 또는
git reset HEAD~1
```

*   commit 취소
*   파일은 **working tree에 남음**
*   👉 다시 add / commit 하고 싶을 때

***

### 🔹 마지막 commit + 변경 내용까지 전부 삭제 ⚠️

```bash
git reset --hard HEAD~1
```

*   commit 완전 삭제
*   파일 변경 내용도 **복구 불가**
*   👉 진짜 필요 없을 때만 사용

***

## 2️⃣ **이미 push 한 commit** 되돌리기 (공유 브랜치)

> ⚠️ **reset + force push는 팀원에게 피해**  
> → 실무에서는 **revert 권장**

***

### ✅ 가장 안전한 방법 (권장)

### 🔹 commit을 취소하는 commit 생성

```bash
git revert <commit-id>
```

예:

```bash
git revert a1b2c3d4
```

*   기존 히스토리는 유지
*   “이 커밋을 취소한다”는 **새 commit** 생성
*   👉 main / develop / release 브랜치에서 표준

***

### 🔹 여러 commit 한 번에 revert

```bash
git revert a1b2c3d4..e5f6g7h8
```

***

## 3️⃣ 이미 push 했지만 **혼자 쓰는 브랜치**라면

> (feature 브랜치, 개인 작업 브랜치)

```bash
git reset --hard HEAD~1
git push -f
```

*   force push 사용
*   👉 **혼자 쓰는 브랜치에서만**

***

## 4️⃣ 특정 파일만 되돌리고 싶을 때

### 🔹 특정 파일을 이전 commit 상태로

```bash
git checkout HEAD~1 -- shop-payment/build/tmp/compileJava/previous-compilation-data.bin
```

또는 (신규 문법)

```bash
git restore --source=HEAD~1 shop-payment/build/tmp/compileJava/previous-compilation-data.bin
```

***

## 5️⃣ 커밋 내역 확인 (되돌릴 대상 찾기)

```bash
git log --oneline --graph --decorate
```

***

## ✅ 실무 기준 추천 판단표

| 상황               | 추천 명령                          |
| ---------------- | ------------------------------ |
| commit 메시지만 잘못됨  | `git reset --soft HEAD~1`      |
| 로컬에서 커밋 자체 취소    | `git reset --mixed HEAD~1`     |
| 변경 내용까지 삭제       | `git reset --hard HEAD~1`      |
| 이미 push + 공유 브랜치 | `git revert <commit>` ✅        |
| 혼자 쓰는 브랜치        | `git reset --hard` + `push -f` |

***

## 💡 지금 상황에 딱 맞는 케이스 (추정)

앞선 대화 맥락상:

*   `.gitignore` 수정
*   `build` 관련 파일 커밋됨
*   **방금 커밋한 걸 되돌리고 싶은 상황**으로 보임

👉 **아직 push 안 했다면**

```bash
git reset --soft HEAD~1
```

👉 **이미 push 했다면**

```bash
git revert HEAD
```

***

원하시면

*   ✅ **“이미 push 했는지 / 안 했는지”**
*   ✅ **main / develop / feature 브랜치 여부**

말씀 주시면 **딱 맞는 명령 한 줄**로 정확히 정리해 드리겠습니다.
