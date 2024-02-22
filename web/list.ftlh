[#ftl output_format="HTML"]
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta http-equiv="X-UA-Compatible" content="IE=edge"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title>Username Form
	</title><link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2/dist/tailwind.min.css" type="text/css"></head>
<body>

  
	
	
	
	
	
  
  
	<div class="md:h-screen bg-white relative flex flex-col justify-center items-center">
	  <div class="md:border md:border-gray-300 bg-white md:shadow-lg shadow-none rounded p-10 align-middle">
		<div class="flex flex-col items-center space-y-3">
		  <div class="flex">
			<img src="https://gluu.org/wp-content/uploads/2020/12/logo.png">
		  </div>
		  <span class="text-2xl font-semi-bold leading-normal"> </span>
		  <p class="leading-normal">Sign in with
		  </p>
		</div>
		<form method="post" class="my-8">
		  <div class="space-y-9">
			<div class="text-sm flex justify-center items-center">
			  [#list providers?keys as id]
			  <button type="submit" name="selection" value="${id}">${id}</button>
			  <br>
			  <br>
			  [/#list]
			</div>
		  </div>
		</form>
	  </div>
	</div>
  
</body>


</html>